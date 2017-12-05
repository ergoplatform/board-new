package org.ergoplatform.board.mongo

import java.util.concurrent.atomic.AtomicInteger

import de.flapdoodle.embed.mongo.config._
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.mongo.{Command, MongodExecutable, MongodStarter}
import de.flapdoodle.embed.process.config.IRuntimeConfig
import de.flapdoodle.embed.process.config.io.ProcessOutput
import de.flapdoodle.embed.process.runtime.Network

trait EmbeddedMongoInstance {

  /**
    * Running multiple mongo tests could lead to the "port is being used" exception.
    * So to avoid this kind of error we going to run each mongo executable instance on a brand new port.
    */
  val port: Int = EmbeddedMongoInstance.port.getAndIncrement()

  val version: Version = Version.V3_4_1
  val bindIp: String = "localhost"
  val isIpv6: Boolean = Network.localhostIsIPv6()
  lazy val mongouri = s"mongodb://$bindIp:$port"

  def createRuntimeConfig(cmd: Command = Command.MongoD,
                          po: ProcessOutput = ProcessOutput.getDefaultInstanceSilent): IRuntimeConfig = {
    new RuntimeConfigBuilder()
      .defaults(cmd)
      .processOutput(po)
      .build()
  }

  def createMongoConfig(rc: IRuntimeConfig,
                        version: Version = version,
                        bindIp: String = bindIp,
                        port: Int = port,
                        isIpv6: Boolean = isIpv6): IMongodConfig = {
    new MongodConfigBuilder()
      .version(version)
      .net(new Net(bindIp, port, isIpv6))
      .build()
  }

  def mongoEx(rc: IRuntimeConfig = createRuntimeConfig(),
              bindIp: String = bindIp,
              port: Int = port,
              version: Version = version,
              isIpv6: Boolean = isIpv6 ): MongodExecutable = {
    val mc = createMongoConfig(rc, version, bindIp, port, isIpv6)
    MongodStarter.getInstance(rc).prepare(mc)
  }
}

object EmbeddedMongoInstance {
  val port = new AtomicInteger(27027)
}
