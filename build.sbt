/*
 * Copyright 2022 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import sbt.*
import uk.gov.hmrc.DefaultBuildSettings.{addTestReportOption, defaultSettings}
import uk.gov.hmrc.SbtAutoBuildPlugin
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion

val appName = "cis-deductions-api"

lazy val ItTest = config("it") extend Test

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) // Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test(),
    retrieveManaged                 := true,
    update / evictionWarningOptions := EvictionWarningOptions.default.withWarnScalaVersionEviction(warnScalaVersionEviction = false),
    scalaVersion                    := "2.13.12",
    scalacOptions ++= List(
      "-language:higherKinds",
      "-Xlint:-byname-implicit",
      "-Xfatal-warnings",
      "-Wconf:src=routes/.*:silent",
      "-feature",
      "-Wconf:cat=other-match-analysis:error",
      "-Wconf:cat=unused-imports&src=routes/.*:s",
      "-Wconf:msg=legacy-binding:s")
  )
  .settings(
    Compile / unmanagedResourceDirectories += baseDirectory.value / "resources"
  )
  .settings(majorVersion := 0)
  .settings(CodeCoverageSettings.settings: _*)
  .settings(defaultSettings(): _*)
  .configs(ItTest)
  .settings(inConfig(ItTest)(
    Defaults.itSettings ++ headerSettings(ItTest) ++ automateHeaderSettings(ItTest) ++ ScalafmtPlugin.scalafmtConfigSettings))
  .settings(
    ItTest / fork                       := true,
    ItTest / unmanagedSourceDirectories := Seq((ItTest / baseDirectory).value / "it"),
    ItTest / unmanagedClasspath += baseDirectory.value / "resources",
    Runtime / unmanagedClasspath += baseDirectory.value / "resources",
    ItTest / javaOptions += "-Dlogger.resource=logback-test.xml",
    ItTest / parallelExecution := false,
    addTestReportOption(ItTest, directory = "int-test-reports")
  )
  .settings(
    resolvers += Resolver.jcenterRepo
  )
  .settings(PlayKeys.playDefaultPort := 7781)

//lazy val it = project
//  .enablePlugins(PlayScala)
//  .dependsOn(microservice % "test->test")
//  .settings(DefaultBuildSettings.itSettings())
//  .settings(libraryDependencies ++=AppDependencies.itDependencies)

dependencyUpdatesFilter -= moduleFilter(name = "bootstrap-backend-play-30")
dependencyUpdatesFilter -= moduleFilter(organization = "org.playframework")
