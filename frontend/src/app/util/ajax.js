import axios from "axios";

const baseUrl = 'http://localhost:8080/v1';


class Ajax {
    get = (url) => {
        return axios.get(baseUrl + url)
    };

    post = (url, content) => {
        return axios.post(baseUrl + url, JSON.stringify(content), {
            validateStatus: status => true
        })
    };
}

export default Ajax;