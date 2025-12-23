/**
 * 日期格式化工具函数
 * @param {Date|String} date - 日期对象/日期字符串
 * @param {String} format - 格式化模板，默认 'yyyy-MM-dd HH:mm:ss'
 * @returns {String} 格式化后的日期字符串
 */
export function formatDate(date, format = 'yyyy-MM-dd HH:mm:ss') {
  if (!date) return '';
  
  // 处理字符串类型的日期
  const targetDate = typeof date === 'string' ? new Date(date) : date;
  
  // 兼容日期解析失败的情况
  if (isNaN(targetDate.getTime())) return '';

  const opt = {
    'y+': targetDate.getFullYear().toString(), // 年
    'M+': (targetDate.getMonth() + 1).toString(), // 月
    'd+': targetDate.getDate().toString(), // 日
    'H+': targetDate.getHours().toString(), // 时
    'm+': targetDate.getMinutes().toString(), // 分
    's+': targetDate.getSeconds().toString() // 秒
  };

  // 替换年
  if (/(y+)/.test(format)) {
    format = format.replace(RegExp.$1, (opt['y+'].substring(4 - RegExp.$1.length)));
  }

  // 替换月、日、时、分、秒
  for (const k in opt) {
    if (new RegExp(`(${k})`).test(format)) {
      format = format.replace(
        RegExp.$1,
        RegExp.$1.length === 1 ? opt[k] : opt[k].padStart(RegExp.$1.length, '0')
      );
    }
  }

  return format;
}