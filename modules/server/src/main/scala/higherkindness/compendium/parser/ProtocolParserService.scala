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

package higherkindness.compendium.parser

import cats.effect.Sync
import higherkindness.compendium.models.DBModels.MetaProtocol
import higherkindness.compendium.models.Target
import higherkindness.compendium.models.parserModels.ParserResult

trait ProtocolParserService[F[_]] {

  def parse(protocol: Option[MetaProtocol], target: Target): F[ParserResult]
}

object ProtocolParserService {

  def apply[F[_]: Sync](implicit F: ProtocolParserService[F]): ProtocolParserService[F] = F

}