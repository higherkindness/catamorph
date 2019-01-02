/*
 * Copyright 2018-2019 47 Degrees, LLC. <http://www.47deg.com>
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

package higherkindness.compendium

import cats.effect.Effect
import fs2.{Stream, StreamApp}
import higherkindness.compendium.models.HttpConfig
import org.http4s.HttpService
import org.http4s.server.blaze.BlazeBuilder

import scala.concurrent.ExecutionContext

object CompendiumServerStream {

  def serverStream[F[_]: Effect](httpConf: HttpConfig, service: HttpService[F])(
      implicit ec: ExecutionContext): Stream[F, StreamApp.ExitCode] = {
    BlazeBuilder[F]
      .bindHttp(httpConf.port, httpConf.host)
      .mountService(service, "/")
      .serve
  }

}
