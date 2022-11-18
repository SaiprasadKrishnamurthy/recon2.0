import axios from 'axios';


const axiosClient = axios.create({
    baseURL: process.env.REACT_APP_API_BASE_URL,
    timeout: 60000,
    headers: {
        'Authorization': 'Bearer ' + localStorage.getItem("apiKey"),
        'Access-Control-Allow-Credentials': 'true'
    }
});

axiosClient.interceptors.request.use(function (config) {
    const token = localStorage.getItem('apiKey');
    config.headers.Authorization = 'Bearer ' + token;
    return config;
});

axiosClient.interceptors.response.use(undefined, err => {
    const status = err.response ? err.response.status : null;
    if (status === 401) {
        if (!window.location.href.includes('reset-password')) {
            window.location.href = '#/api-key'
        }
    }
});

export default axiosClient;