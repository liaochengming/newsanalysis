package com.kunyan.util

import java.sql.{Connection, DriverManager}
import java.util.Properties

import com.kunyandata.nlpsuit.deduplication.TitleDeduplication
import kafka.producer.{KeyedMessage, Producer, ProducerConfig}
import org.apache.hadoop.hbase.{HBaseConfiguration, TableName}
import redis.clients.jedis.{Jedis, JedisPool, JedisPoolConfig}

import scala.xml.Elem
import java.util


class LazyConnections(createHbaseConnection: () => org.apache.hadoop.hbase.client.Connection,
                      createProducer: () => Producer[String, String],
                      createMySQLNewsConnection: () => Connection,
                      createJedis: () => Jedis
                       ) extends Serializable {


  lazy val hbaseConn = createHbaseConnection()

  lazy val mysqlNewsConn = createMySQLNewsConnection()

  lazy val producer = createProducer()

  lazy val jedis = createJedis()


  def sendTask(topic: String, value: String): Unit = {

    val message = new KeyedMessage[String, String](topic, value)

    try {
      producer.send(message)
    } catch {
      case e: Exception =>
        e.printStackTrace()
    }
  }

  def sendTask(topic: String, values: Seq[String]): Unit = {

    val messages = values.map(x => new KeyedMessage[String, String](topic, x))

    try {
      producer.send(messages: _*)
    } catch {
      case e: Exception =>
        e.printStackTrace()
    }
  }

  def getTable(tableName: String) = hbaseConn.getTable(TableName.valueOf(tableName))

  def jedisExists(key: String): Boolean = {
    jedisConnectIfNot()
    jedis.exists(key)
  }

  def jedisConnectIfNot(): Unit = {
    if (!jedis.isConnected) {
      jedis.connect()
      println("redis reconnect!!!")
    }
  }

  def jedisGetKeysLike(keys: String): util.Set[String] = {
    jedis.keys(keys)
  }

  def existSimilarKey(title: String): Boolean = {

    val keys = jedisGetKeysLike("news:*")
    val iterator = keys.iterator()

    while (iterator.hasNext) {
      val item = iterator.next()
      if (TitleDeduplication.process(title, item, 2, 0.6))
        return true
    }

    false
  }

  def jedisSet(key: String, value: String, overTime: Int): String = {
    jedisConnectIfNot()
    val result = jedis.set(key, value)
    jedis.expire(key, overTime)
    result
  }

}

object LazyConnections {

  def apply(configFile: Elem): LazyConnections = {


    val createHbaseConnection = () => {

      val hbaseConf = HBaseConfiguration.create
      hbaseConf.set("hbase.rootdir", (configFile \ "hbase" \ "rootDir").text)
      hbaseConf.set("hbase.zookeeper.quorum", (configFile \ "hbase" \ "ip").text)
      println("create connection")

      val connection = org.apache.hadoop.hbase.client.ConnectionFactory.createConnection(hbaseConf)

      sys.addShutdownHook {
        connection.close()
      }

      println("Hbase connection created.")

      connection
    }

    val createProducer = () => {

      val props = new Properties()
      props.put("metadata.broker.list", (configFile \ "kafka" \ "brokerList").text)
      props.put("serializer.class", "kafka.serializer.StringEncoder")
      props.put("producer.type", "async")

      val config = new ProducerConfig(props)
      val producer = new Producer[String, String](config)

      sys.addShutdownHook {
        producer.close()
      }

      producer
    }

    //连接新闻表
    val createMySQLNewsConnection = () => {

      Class.forName("com.mysql.jdbc.Driver")
      val connection = DriverManager.getConnection((configFile \ "mysqlNews" \ "url").text, (configFile \ "mysqlNews" \ "username").text, (configFile \ "mysqlNews" \ "password").text)

      sys.addShutdownHook {
        connection.close()
      }

      connection
    }

    val createJedis = () => {

      val config: JedisPoolConfig = new JedisPoolConfig
      config.setMaxWaitMillis(10000)
      config.setMaxIdle(10)
      config.setMaxTotal(1024)
      config.setTestOnBorrow(true)

      val jedisPool = new JedisPool(config, (configFile \ "redis" \ "ip").text,
        (configFile \ "redis" \ "port").text.toInt, 20000,
        (configFile \ "redis" \ "auth").text, (configFile \ "redis" \ "db").text.toInt)

      sys.addShutdownHook {
        jedisPool.close()
      }

      jedisPool.getResource
    }

    new LazyConnections(createHbaseConnection, createProducer, createMySQLNewsConnection, createJedis)

  }

}
