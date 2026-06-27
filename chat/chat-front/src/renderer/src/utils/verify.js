const regs = {
  email: /^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\.[a-zA-Z0-9-.]+$/,
  password: /^\w{8,18}$/,
  version: /^[0-9.]+$/,
  number: /^\+?[0-9]*$/
}

const verify = (rule, value, reg, callback) => {
  if (!value) {
    callback()
    return
  }
  if (reg.test(value)) {
    callback()
  } else {
    callback(new Error(rule.message))
  }
}

const checkPassword = (value) => {
  return regs.password.test(value)
}

const checkEmail = (value) => {
  return regs.email.test(value)
}

const password = (rule, value, callback) => {
  return verify(rule, value, regs.password, callback)
}

const number = (rule, value, callback) => {
  return verify(rule, value, regs.number, callback)
}

const email = (rule, value, callback) => {
  return verify(rule, value, regs.email, callback)
}

const version = (rule, value, callback) => {
  return verify(rule, value, regs.version, callback)
}

export default {
  checkPassword,
  checkEmail,
  password,
  number,
  version,
  email
}
