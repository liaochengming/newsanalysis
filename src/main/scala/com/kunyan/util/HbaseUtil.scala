package com.kunyan.util

import com.ibm.icu.text.CharsetDetector
import org.apache.hadoop.hbase.client.Get
import org.apache.hadoop.hbase.util.Bytes

/**
 * Created by Administrator on 2017/7/17.
 *
 */
object HbaseUtil {


  /**
   * 根据表名和rowkey从hbase中获取数据
   *
   * @param tableName 表名
   * @param rowkey    索引
   * @param lazyConn  连接容器
   * @return (url, html)
   */
  def query(tableName: String, rowkey: String, lazyConn: LazyConnections): (String, String,String,String) = {

    val table = lazyConn.getTable(tableName)
    val get = new Get(rowkey.getBytes)

    try {

      val time = table.get(get).getValue(Bytes.toBytes("basic"), Bytes.toBytes("time"))
      val content = table.get(get).getValue(Bytes.toBytes("basic"), Bytes.toBytes("content"))
      val title = table.get(get).getValue(Bytes.toBytes("basic"), Bytes.toBytes("title"))
      val timeSpider = table.get(get).getValue(Bytes.toBytes("basic"),Bytes.toBytes("time_spider"))
      val articleType = table.get(get).getValue(Bytes.toBytes("basic"),Bytes.toBytes("article_type"))


      if (time == null && content == null &&title == null) {
        println(s"Get empty data by this table: $tableName and rowkey: $rowkey")
        return null
      }

      val encoding = new CharsetDetector().setText(content).detect().getName

      if(new String(time, "UTF-8") == ""){
        (new String(timeSpider, "UTF-8"), new String(content, encoding), new String(title, encoding),new String(articleType,encoding))
      }else{
        (new String(time, "UTF-8"), new String(content, encoding), new String(title, encoding),new String(articleType,encoding))
      }
    } catch {

      case e: Exception =>
        e.printStackTrace()
        null

    }

  }
}
