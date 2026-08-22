import http from 'k6/http';
import { check } from 'k6';
import { sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const USERNAME = __ENV.K6_USERNAME;
const PASSWORD = __ENV.K6_PASSWORD;

export const options = {
    stages: [
        { duration: '30s', target: 10 },
        { duration: '30s', target: 20 },
        { duration: '30s', target: 30 },
        { duration: '30s', target: 40 },
        { duration: '30s', target: 50 },
        { duration: '30s', target: 0 },
    ],

    thresholds: {
        http_req_failed: ['rate<0.01'],
    },
};

export function setup() {
    if (!USERNAME || !PASSWORD) {
        throw new Error(
            'K6_USERNAME and K6_PASSWORD environment variables are required.'
        );
    }

    const payload = JSON.stringify({
        username: USERNAME,
        password: PASSWORD,
    });

    const response = http.post(
        `${BASE_URL}/api/v1/auth/login`,
        payload,
        {
            headers: {
                'Content-Type': 'application/json',
            },
            tags: {
                name: 'POST /api/v1/auth/login',
            },
        }
    );

    const loginSuccessful = check(response, {
        'login status is 200': (r) => r.status === 200,
        'access token exists': (r) => {
            try {
                return Boolean(r.json('accessToken'));
            } catch {
                return false;
            }
        },
    });

    if (!loginSuccessful) {
        throw new Error(
            `Login failed. Status: ${response.status}, Body: ${response.body}`
        );
    }

    return {
        accessToken: response.json('accessToken'),
    };
}

export default function (data) {
    const response = http.get(
        `${BASE_URL}/api/v1/products?name=phone&page=0&size=20`,
        {
            headers: {
                Authorization: `Bearer ${data.accessToken}`,
            },
            tags: {
                name: 'GET /api/v1/products?name=phone',
            },
        }
    );

    check(response, {
        'products status is 200': (r) => r.status === 200,
        'response has content': (r) => r.body.length > 0,
    });

    sleep(0.1);
}
