/**
 * Created by xuefengwang on 2018/1/25.
 */
public enum ClientTextType {
  /// <summary>
  /// 文本类型
  /// </summary>
  Text(1),
  /// <summary>
  /// 超链
  /// </summary>
  Href (2),
  /// <summary>
  /// 表情
  /// </summary>
  Expression (3) ,
  /// <summary>
  /// 图片
  /// </summary>
  Photo (4) ,
  /// <summary>
  /// 电话
  /// </summary>
  Tel (5) ,
  /// <summary>
  /// 邮件
  /// </summary>
  Email(6);
  private int value = 0;
  private ClientTextType(int value){
    this.value =value;
  }
}
