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

package higherkindness.compendium.http

import cats.effect.Sync
import cats.syntax.functor._
import cats.syntax.flatMap._
import org.http4s._
import higherkindness.compendium.db.DBService
import higherkindness.compendium.storage.Storage
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Location
import org.http4s.multipart.Multipart

object RootService {

  def rootRouteService[F[_]: Sync: Storage: DBService]: HttpService[F] = {

    object f extends Http4sDsl[F]
    import f._

    val utils = HttpUtils[F]
    HttpService[F] {
      case GET -> Root / "ping" => Ok("pong")

      case req @ POST -> Root / "v0" / "protocol" =>
        req.decode[Multipart[F]] { m =>
          val act = for {
            name     <- utils.filename(m)
            text     <- utils.rawText(m)
            protocol <- utils.protocol(name, text)
            id       <- DBService[F].addProtocol(protocol)
            _        <- Storage[F].store(id, protocol)
          } yield id

          Sync[F].recoverWith(
            act
              .flatMap(id =>
                Ok(s"$id").map(_.putHeaders(Location(req.uri.withPath(s"${req.uri.path}/$id")))))) {
            case e: org.apache.avro.SchemaParseException => BadRequest(e.getMessage)
            case e =>
              e.printStackTrace()
              InternalServerError()
          }
        }

      case GET -> Root / "v0" / "protocol" / IntVar(protocolId) =>
        Storage[F]
          .recover(protocolId)
          .flatMap(_.fold(NotFound())(p => Ok(p.raw)))

    }
  }
}