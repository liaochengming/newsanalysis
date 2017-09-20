package com.kunyan.conf

/**
  * Created by yangshuai on 2016/3/31.
  */
object Platform extends Enumeration {

  val ThsStock = Value(1, "同花顺股票")  //http://www.10jqka.com.cn
  val ZhiHu = Value(2, "知乎")  //http://www.zhihu.com
  val YiCai = Value(3, "第一财经")  //http://www.yicai.com
  val WeiBo = Value(4, "微博")  //http://weibo.com
  val CN = Value(5, "21CN")  //http://www.21cn.com
  val ThsNews = Value(6, "同花顺新闻")  //http://www.10jqka.com.cn
  val SnowBall = Value(7, "雪球")  //http://xueqiu.com
  val DaZhihui = Value(8, "大智慧")  //http://www.gw.com.cn
  val EastMoney = Value(9, "东方财富")  //http://www.eastmoney.com
  val Government = Value(10, "政府网")
  val QuanJing = Value(11, "全景网")  //http://www.p5w.net
  val HeXun = Value(12, "和讯")  //http://www.hexun.com
  val StockStar = Value(13, "证券之星")  //http://www.stockstar.com
  val CaiJing = Value(14, "财经网")  //http://www.caijing.com.cn
  val JRJ = Value(15, "金融界")  //www.jrj.com.cn
  val CFI = Value(16, "中国财经信息网")  //http://www.cfi.net.cn
  val ZhongZheng = Value(17, "中证网")  //http://www.cs.com.cn
  val CNStock = Value(18, "上海证券报")  //http://www.cnstock.com
  val STCN = Value(19, "证券时报网·中国")  //http://www.stcn.com
  val XinHua = Value(20, "新华网财经")  //http://www.news.cn/fortune
  val FengHuang = Value(21, "凤凰财经")  //http://finance.ifeng.com
  val SINA = Value(22, "新浪财经")  //http://finance.sina.com.cn
  val SouHu = Value(23, "搜狐财经")  //http://business.sohu.com
  val NetEase = Value(24, "网易财经")  //http://money.163.com
  val WallStreet = Value(25, "华尔街见闻")  //http://wallstreetcn.com
  val Tencent = Value(26,"腾讯财经") //http://finance.qq.com/
  val ChinaCom = Value(27,"中国网") //http://www.china.com.cn/
  val InternationalFinance = Value(28,"国际金融报") //http://www.gfic.cn/optinFin.shtml
  val GlobalTigerNetwork = Value(29,"环球老虎网")
  val SuperiorWealth = Value(30,"优品财富")
  val SmartFinanceNetwork = Value(31,"智通财经网 ")
  val ChinaYangNetwork = Value(32,"中青网")
  val CaiLianPressNews = Value(10001, "财联社新闻")  //http://www.cailianpress.com
  val IGOLDENBETA_SELF_MEDIA = Value(30004, "金贝塔")  //http://www.igoldenbeta.com:8080/cn-jsfund-server-mobile/bkt/api?imsi=46d4ba19332350f63b9bfa7621b4924e&deviceId=46d4ba19332350f63b9bfa7621b4924e&type=originaljson&cid=AppStore&imei=46d4ba19332350f63b9bfa7621b4924e&t=1461652249&data=%7B%0A++%22cursor%22+%3A+%22-1%22%2C%0A++%22order%22+%3A+%220%22%2C%0A++%22count%22+%3A+%2220%22%0A%7D&sid=&appKey=29129215&api=api.system.feed.list&ttid=iPhone+OS_9.3.1_iPhone_basket_AppStore_wifi_20160426143049334%2C46d4ba19332350f63b9bfa7621b4924e%2C9621_2.4.0_v6105&v=1.0&sign=bab97f036e79747e5867382e924608bb
  val WECHAT_SELF_MEDIA = Value(30006, "微信")  //http://weixin.sogou.com/weixin?type=1&query=QuantitativeHistory&ie=utf8 量化历史研究       //http://weixin.sogou.com/weixin?type=1&query=woshijinsanban&ie=utf8  金三板
  val SHAGNHAI_STOCKEXCHANGE = Value(50001, "上海证券交易所")
  val SHENZHEN_STOCKEXCHANGE = Value(50002, "深圳证券交易所")
  val CNINFO = Value(50003,"巨潮资讯")
  val AIYANBAO = Value(60001, "爱研报")
  val HeXunYanBao = Value(60012,"和讯研报")   //http://yanbao.stock.hexun.com/
  val EastMoneyYanBao = Value(60013,"东方财富研报")    //http://data.eastmoney.com/report/
}
