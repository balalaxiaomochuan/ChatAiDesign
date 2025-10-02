package com.huawei.chataidesign.config.generator;

import java.util.Random;

public class NumberGenerateor {
    public static Long generateRandomLongNumber(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();

        // 第一位不能为0，所以取1-9之间的数字
        sb.append(random.nextInt(9) + 1);

        // 后面19位可以是0-9之间的任意数字
        for (int i = 0; i < length - 1; i++) {
            sb.append(random.nextInt(10));
        }

        return Long.parseLong(sb.toString());
    }
}
