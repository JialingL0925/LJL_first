/**
 * 权限工具函数
 * 定义各角色的菜单权限
 */

// 角色常量
export const ROLE = {
  ADMIN: 'ADMIN',           // 管理员
  ACCOUNTANT: 'ACCOUNTANT', // 会计
  PURCHASER: 'PURCHASER',   // 采购
  CASHIER: 'CASHIER',       // 出纳
  SALES: 'SALES'            // 销售
}

// 菜单权限配置
const MENU_PERMISSIONS = {
  [ROLE.ADMIN]: [
    '/home',
    '/supplier',
    '/customer',
    '/accountingSubject',
    '/purchaseOrder',
    '/salesOrder',
    '/journalEntry',
    '/posting',
    '/apPayment',
    '/arPayment',
    '/report',
    '/project',
    '/employee'
  ],
  [ROLE.ACCOUNTANT]: [
    '/home',
    '/accountingSubject',
    '/journalEntry',
    '/posting',
    '/report'
  ],
  [ROLE.PURCHASER]: [
    '/home',
    '/supplier',
    '/purchaseOrder'
  ],
  [ROLE.CASHIER]: [
    '/home',
    '/apPayment',
    '/arPayment',
    '/report'
  ],
  [ROLE.SALES]: [
    '/home',
    '/customer',
    '/salesOrder'
  ]
}

/**
 * 检查用户是否有权限访问指定路径
 * @param {string} path 路径
 * @param {string} role 角色名称
 * @returns {boolean}
 */
export function hasPermission(path, role) {
  if (!role) {
    role = localStorage.getItem('role') || ROLE.ADMIN
  }
  
  const permissions = MENU_PERMISSIONS[role] || []
  const hasAccess = permissions.includes(path)
  console.log('🔍 hasPermission检查 - 路径：', path, '角色：', role, '权限列表：', permissions, '结果：', hasAccess)
  return hasAccess
}

/**
 * 获取当前用户角色
 * @returns {string}
 */
export function getCurrentRole() {
  return localStorage.getItem('role') || ROLE.ADMIN
}

/**
 * 检查是否为管理员（基于角色ID，roleId为1表示管理员）
 * @returns {boolean}
 */
export function isAdmin() {
  const roleId = localStorage.getItem('roleId')
  // 角色ID为1表示管理员
  return roleId === '1' || roleId === 1 || getCurrentRole() === ROLE.ADMIN
}

/**
 * 获取用户可访问的菜单列表
 * @param {string} role 角色名称
 * @returns {Array}
 */
export function getAccessibleMenus(role) {
  if (!role) {
    role = getCurrentRole()
  }
  
  // 清理角色值（去除空格等）
  role = String(role).trim()
  
  console.log('🔍 getAccessibleMenus调用 - 角色：', role, '角色类型：', typeof role, '角色值长度：', role.length)
  console.log('🔍 可用角色键：', Object.keys(MENU_PERMISSIONS))
  console.log('🔍 角色匹配检查：', {
    'ADMIN': role === 'ADMIN',
    'ACCOUNTANT': role === 'ACCOUNTANT',
    'PURCHASER': role === 'PURCHASER',
    'CASHIER': role === 'CASHIER',
    'SALES': role === 'SALES'
  })
  
  const allMenus = [
    { path: '/home', title: '系统首页', icon: 'House' },
    { path: '/supplier', title: '供应商管理', icon: 'UserFilled' },
    { path: '/customer', title: '客户管理', icon: 'User' },
    { path: '/accountingSubject', title: '会计科目管理', icon: 'Wallet' },
    { path: '/purchaseOrder', title: '采购订单', icon: 'ShoppingCart' },
    { path: '/salesOrder', title: '销售订单', icon: 'Ticket' },
    { path: '/journalEntry', title: '分录管理', icon: 'Document' },
    { path: '/posting', title: '凭证过账', icon: 'Check' },
    { path: '/apPayment', title: '付款管理', icon: 'Money' },
    { path: '/arPayment', title: '收款管理', icon: 'CreditCard' },
    { path: '/report', title: '报表管理', icon: 'DataAnalysis' },
    { path: '/project', title: '项目管理', icon: 'Folder' },
    { path: '/employee', title: '员工管理', icon: 'UserFilled' }
  ]
  
  const permissions = MENU_PERMISSIONS[role] || []
  console.log('🔍 权限配置 - 角色：', role, '权限列表：', permissions, '权限数量：', permissions.length)
  
  if (permissions.length === 0) {
    console.error('❌ 未找到角色权限配置！角色：', role, '可用角色：', Object.keys(MENU_PERMISSIONS))
    console.error('❌ 如果角色不匹配，将返回空菜单列表')
    // 如果角色不匹配，返回空数组而不是所有菜单
    return []
  }
  
  const filteredMenus = allMenus.filter(menu => permissions.includes(menu.path))
  console.log('🔍 菜单过滤结果 - 原始菜单数：', allMenus.length, '过滤后菜单数：', filteredMenus.length)
  console.log('🔍 过滤后的菜单：', filteredMenus.map(m => `${m.title}(${m.path})`))
  
  return filteredMenus
}

