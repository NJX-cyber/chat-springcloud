import axios from "axios"
import { ElLoading } from "element-plus"
import Message from './message'
import Confirm from './confirm'
import Api from './api'
const contentTypeForm = "application/x-www-form-urlencoded;charset=UTF-8"
const contentTypeJson = "application/json;charset=UTF-8"
const responseTypeJson = "json"

let loading = null

const instance = axios.create({
    withCredentials: true,
    baseURL: (import.meta.env.PROD ? Api.prodDomain : ""),
    timeout: 4000,
})

//请求前拦截器
instance.interceptors.request.use(
    (config) => {
        if (config.showLoading) {
            loading = ElLoading.service({
                lock: true,
                text: "Loading",
                background: "rgba(0, 0, 0, 0.7)",
            })
        }
        return config
    },
    (error) => {
        if (config.showLoading && loading) {
            loading.close()
        }
        Message.error("请求错误")
        return Promise.reject(error)
    }
)

//请求后拦截
instance.interceptors.response.use(
    (response) => {
        const { showLoading, errorCallback, showError = true, responseType } = response.config
        if (showLoading && loading) {
            loading.close()
        }
        const responseData = response.data
        if (responseType === "blob" || responseType === "arraybuffer") {
            return responseData
        }

        // 正常请求
        if (responseData.code === 200) {
            return responseData
        } else if (responseData.code === 901) {
            Confirm({
                message: "登录超时，请重新登录",
                okfun: () => {
                    setTimeout(() => {
                        window.ipcRenderer.send("reLogin");
                    }, 1000);
                },
                showCancelButton: false
            });
            return;
        } else {
            // 其他错误
            if (errorCallback) {
                errorCallback(responseData)
            }
            return Promise.reject({ showError: showError, msg: "网络异常"})
        }
    },
    (error) => {
        if (error.config.showLoading && loading) {
            loading.close()
        }
        return Promise.reject({ showError: true, msg: "网络异常" })
    }
)

const request = (config) => {
    const { url, params, dataType, method = "post", showLoading = true, responseType = responseTypeJson, showError = true } = config
    let contentType = contentTypeForm
    let formData = new FormData() //创建formData对象
    for (let key in params) {
        formData.append(key, params[key] == undefined ? "" : params[key]) //将参数添加到formData对象中
    }
    if (dataType != null && dataType === "json") {
        contentType = contentTypeJson
    }
    const token = localStorage.getItem("token")
    let headers = {
        "X-Requested-With": "XMLHttpRequest",
        "token": token ? token : ""
    }
    // FormData 不能手动设置 Content-Type，让 axios 自动生成 boundary
    if (dataType == null || dataType !== "json") {
        // 非 JSON 请求不覆盖 Content-Type，axios 自动处理
    } else {
        headers["Content-Type"] = contentTypeJson
    }

    if (method.toLowerCase() === "get") {
        return instance.get(url, {
            params: params,
            headers: headers,
            showLoading: showLoading,
            errorCallback: config.errorCallback,
            responseType: responseType,
            showError: showError
        }).catch((error) => {
            if (error.showError) {
                Message.error(error.msg)
            }
            return null
        })
    }
    if (method.toLowerCase() === "put") {
        return instance.put(url, formData, {
            headers: headers,
            showLoading: showLoading,
            errorCallback: config.errorCallback,
            responseType: responseType,
            showError: showError
        }).catch((error) => {
            if (error.showError) {
                Message.error(error.msg)
            }
            return null
        })
    }

    if (method.toLowerCase() === "delete") {
        return instance.delete(url, {
            params: params,
            headers: headers,
            showLoading: showLoading,
            errorCallback: config.errorCallback,
            responseType: responseType,
            showError: showError
        }).catch((error) => {
            if (error.showError) {
                Message.error(error.msg)
            }
            return null
        })
    }

    if (method.toLowerCase() === 'post') {
        return instance.post(url, formData, {
            headers: headers,
            showLoading: showLoading,
            errorCallback: config.errorCallback,
            responseType: responseType,
            showError: showError
        }).catch((error) => {
            if (error.showError) {
                Message.error(error.msg)
            }
            return null
        })
    }
}

export default request