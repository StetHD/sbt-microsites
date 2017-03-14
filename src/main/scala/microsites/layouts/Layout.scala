/*
 * Copyright 2016 47 Degrees, LLC. <http://www.47deg.com>
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

package microsites.layouts

import microsites.MicrositeSettings
import microsites.util.FileHelper._
import microsites.util.MicrositeHelper

import scalatags.Text.TypedTag
import scalatags.Text.all._
import scalatags.Text.tags2.{nav, title}

abstract class Layout(config: MicrositeSettings) {
  implicitly(config)

  lazy val micrositeHelper = new MicrositeHelper(config)

  def render: TypedTag[String]

  def commonHead: TypedTag[String] = {
    head(
      metas,
      favicons,
      styles
    )
  }

  def metas: List[TypedTag[String]] =
    List(
      meta(charset := "utf-8"),
      meta(httpEquiv := "X-UA-Compatible", content := "IE=edge,chrome=1"),
      title(config.identity.name),
      meta(name := "viewport", content := "width=device-width, initial-scale=1.0"),
      meta(name := "description", content := config.identity.description),
      meta(name := "author", content := config.identity.author),
      meta(name := "og:image", content := "{{site.url}}{{site.baseurl}}/img/poster.png"),
      meta(name := "og:title", content := config.identity.name),
      meta(name := "og:site_name", content := config.identity.name),
      meta(name := "og:url", content := config.identity.homepage),
      meta(name := "og:type", content := "website"),
      meta(name := "og:description", content := config.identity.description),
      meta(name := "twitter:image", content := "{{site.url}}{{site.baseurl}}/img/poster.png"),
      meta(name := "twitter:card", content := "summary_large_image"),
      meta(name := "twitter:site", content := config.identity.twitter),
      meta(
        name := "kazari-dependencies",
        content :=
          config.micrositeKazariSettings.micrositeKazariDependencies
            .map(dependency =>
              s"${dependency.groupId};${dependency.artifactId}_${dependency.scalaVersion};${dependency.version}")
            .mkString(",")),
      meta(name := "kazari-resolvers",
           content :=
             config.micrositeKazariSettings.micrositeKazariResolvers.mkString(",")),
      link(rel := "icon",
           `type` := "image/png",
           href := "{{site.url}}{{site.baseurl}}/img/favicon.png"))

  def favicons: List[TypedTag[String]] =
    (if (config.visualSettings.favicons.nonEmpty) {
       config.visualSettings.favicons
     } else {
       micrositeHelper.faviconDescriptions
     }).map {
      case icon =>
        link(rel := "icon",
             `type` := "image/png",
             attr("sizes") := s"${icon.sizeDescription}",
             href := s"{{site.url}}{{site.baseurl}}/img/${icon.filename}")
    }.toList

  def styles: List[TypedTag[String]] = {

    val customCssList = fetchFilesRecursively(config.fileLocations.micrositeCssDirectory,
                                              List("css")) map { css =>
      link(rel := "stylesheet", href := s"{{site.baseurl}}/css/${css.getName}")
    }

    val customCDNList = config.fileLocations.micrositeCDNDirectives.cssList map { css =>
      link(rel := "stylesheet", href := css)
    }

    List(
      link(rel := "stylesheet",
           href := "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css"),
      link(rel := "stylesheet",
           href := "https://maxcdn.bootstrapcdn.com/font-awesome/4.7.0/css/font-awesome.min.css"),
      link(
        rel := "stylesheet",
        href := s"{{site.url}}{{site.baseurl}}/highlight/styles/${config.visualSettings.highlightTheme}.css"),
      link(rel := "stylesheet", href := s"{{site.baseurl}}/css/style.css"),
      link(rel := "stylesheet", href := s"{{site.baseurl}}/css/palette.css"),
      link(rel := "stylesheet", href := s"{{site.baseurl}}/css/codemirror.css"),
      link(rel := "stylesheet", href := s"{{site.baseurl}}/css/kazari-style.css"),
      link(
        rel := "stylesheet",
        href := s"{{site.baseurl}}/css/${config.micrositeKazariSettings.micrositeKazariCodeMirrorTheme}.css")
    ) ++ customCssList ++ customCDNList
  }

  def scripts: List[TypedTag[String]] = {

    val customJsList = fetchFilesRecursively(config.fileLocations.micrositeJsDirectory, List("js")) map {
      js =>
        script(src := s"{{site.url}}{{site.baseurl}}/js/${js.getName}")
    }

    val customCDNList = config.fileLocations.micrositeCDNDirectives.jsList map { js =>
      script(src := js)
    }

    List(
      script(src := "https://cdnjs.cloudflare.com/ajax/libs/jquery/1.11.3/jquery.min.js"),
      script(
        src := "https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/3.3.7/js/bootstrap.min.js"),
      script(src := "{{site.url}}{{site.baseurl}}/highlight/highlight.pack.js"),
      script("""hljs.configure({
               |languages:['scala','java','bash']
               |});
               |hljs.initHighlighting();
             """.stripMargin)
    ) ++ customJsList ++ customCDNList
  }

  def kazariEnableScript: TypedTag[String] = script(s"""
      |$$(document).ready(function() {
      |	kazari.KazariPlugin().decorateCode('${config.micrositeKazariSettings.micrositeKazariEvaluatorUrl}/eval', '${config.micrositeKazariSettings.micrositeKazariEvaluatorToken}', '${config.micrositeKazariSettings.micrositeKazariGithubToken}', '${config.micrositeKazariSettings.micrositeKazariCodeMirrorTheme}')
      |})
    """.stripMargin)

  def globalFooter =
    footer(id := "site-footer",
           div(cls := "container",
               div(cls := "row",
                   div(cls := "col-xs-6",
                       p("{{ site.name }} is designed and developed by ",
                         a(href := s"${config.identity.homepage}",
                           target := "_blank",
                           s"${config.identity.author}"))),
                   div(cls := "col-xs-6",
                       p(cls := "text-right",
                         a(href := config.gitSiteUrl,
                           span(cls := s"fa ${config.gitHostingIconClass}"),
                           s"View on ${config.gitSettings.gitHostingService}"))))))

  def buildCollapseMenu: TypedTag[String] =
    nav(cls := "text-right",
        ul(cls := "",
           li(
             a(href := config.gitSiteUrl,
               i(cls := s"fa ${config.gitHostingIconClass}"),
               span(cls := "hidden-xs", config.gitSettings.gitHostingService.name))
           ),
           if (!config.urlSettings.micrositeDocumentationUrl.isEmpty)
             li(
               a(href := s"${config.urlSettings.micrositeDocumentationUrl}",
                 i(cls := "fa fa-file-text"),
                 span(cls := "hidden-xs", "Documentation"))
             )
           else ()))
}
