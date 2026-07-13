/**
 * Centralized icon mapping.
 * Using emojis for now — can be replaced with Lucide icons later.
 */
export const ICONS = {
  // Navigation
  home: '🏠',
  recharge: '⚡',
  plans: '📋',
  history: '🕐',
  notifications: '🔔',
  profile: '👤',
  admin: '🛡️',
  logout: '⏻',

  // Actions
  search: '🔍',
  add: '➕',
  delete: '🗑️',
  edit: '✏️',
  check: '✅',
  cross: '❌',
  info: 'ℹ️',

  // Categories
  data: '📶',
  talktime: '📞',
  unlimited: '♾️',
  all: '🌐',

  // Status
  success: '✅',
  failed: '❌',
  pending: '⏳',
  sent: '📧',

  // Misc
  mobile: '📱',
  operator: '📡',
  payment: '💳',
  email: '📧',
  calendar: '📅',
  bolt: '⚡'
} as const;
