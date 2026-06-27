// request.js
import axios from 'axios';
import store from "./store";

const service = axios.create({
    timeout: 60000,
});

service.interceptors.request.use(
    (config) => {
        const token = store.getUserData("token");
        if (token) {
            config.headers['token'] = token;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

service.interceptors.response.use(
    (response) => {
        if (response.config.responseType === 'stream') {
            return response;
        }

        const res = response.data;
        if (res.code && res.code !== 200) { 
            console.error(`[后端业务异常] Code: ${res.code}, Msg: ${res.msg || res.info}`);
            // if (res.code === 602) {
            //     比如 602 是某种特定的业务异常
            // }
            return Promise.reject(res); // 抛出异常
        }
        return res;
    },
    (error) => {
        console.error('[HTTP 请求异常]:', error);
        return Promise.reject(error);
    }
);

export const uploadRequest = (url, formData) => {
    return service.post(url, formData, {
        headers: {
            "Content-Type": "multipart/form-data"
        }
    });
};

export const downloadStreamRequest = async (url, params) => {
    const response = await service.get(url, {
        params,
        responseType: 'stream'
    });

    const contentType = response.headers['content-type'];
    
    if (contentType && contentType.includes('application/json')) {
        return new Promise((resolve, reject) => {
            let data = '';
            response.data.on('data', chunk => {
                data += chunk.toString();
            });
            response.data.on('end', () => {
                try {
                    const json = JSON.parse(data);
                    reject(json);
                } catch (e) {
                    reject(e);
                }
            });
        });
    }
    return response.data; 
};

export default service;