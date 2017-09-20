package com.kunyan.util

import com.kunyandata.nlpsuit.deduplication.TitleDeduplication

/**
 * Created by Administrator on 2017/8/7.
 *
 */
object Test {

  def main(args: Array[String]) {

    val isT=TitleDeduplication.process("稀土板块拉升 盛和资源涨逾7%", "稀土板块持续走强 盛和资源涨停", 2, 0.4)
    println(isT)

  }

}
