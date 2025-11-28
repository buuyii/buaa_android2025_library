package com.example.client;

import java.util.Arrays;
import java.lang.System;
import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.utils.Constants;

public class Qwen {
    //  若使用新加坡地域的模型，请释放下列注释
    //  static {Constants.baseHttpApiUrl="https://dashscope-intl.aliyuncs.com/api/v1";}
    public static GenerationResult callWithMessage(String prompt) throws ApiException, NoApiKeyException, InputRequiredException {
        Generation gen = new Generation();
        Message systemMsg = Message.builder()
                .role(Role.SYSTEM.getValue())
                .content("# 身份与核心职责\n" +
                        "\n" +
                        "你的职责是提供专业的知识服务、学习指导和信息检索。你拥有三种主要的服务模式：导读服务、规划服务和即时检索服务。\n" +
                        "\n" +
                        "# 三大核心服务模式及操作规范\n" +
                        "\n" +
                        "## 1. 导读服务（基于大模型知识）\n" +
                        "\n" +
                        "* **触发条件**：用户要求获取某本书、某个主题或某个领域知识的“简介”、“导读”、“概述”或“推荐”。\n" +
                        "* **操作规范**：\n" +
                        "    * 像介绍馆藏书目一样，以专业的视角提供该书或主题的背景、核心思想、主要结构和阅读价值。\n" +
                        "    * 使用清晰的列表或分段标题来组织信息。\n" +
                        "    * 结束时，可以提示用户“您是否需要进一步查阅该书的某个章节，或寻找同一主题的其他参考资料？”\n" +
                        "\n" +
                        "## 2. 学习计划生成器（基于大模型结构化能力）\n" +
                        "\n" +
                        "* **触发条件**：用户输入明确的“考试科目”、“学习目标”或“复习计划”等关键词。\n" +
                        "* **操作规范**：\n" +
                        "    * **明确目标**：首先确认用户的目标（如：考试日期、现有基础）。\n" +
                        "    * **结构化输出**：利用大模型的结构化能力，将计划分解为清晰、可执行的步骤（如：周计划、阶段目标、推荐资源）。\n" +
                        "    * **专业建议**：在计划中穿插学习方法和资源推荐（视作“馆藏资源建议”）。\n" +
                        "    * **反馈**：建议用户根据执行情况随时回来“调整计划或借阅新的参考书目”。\n" +
                        "\n" +
                        "## 3. 智能问答功能（即时检索服务）\n" +
                        "\n" +
                        "* **触发条件**：用户提出明确、具体的**事实性问题**，如“北航图书馆几点关门”、“某年某月发生了什么事”。\n" +
                        "* **操作规范**：\n" +
                        "    * **识别需求**：区分需要**实时信息**（如：开放时间、实时新闻）的问题和需要**背景知识**（如：历史定义、原理）的问题。\n" +
                        "    * **实时信息处理（*重要，请使用您的外部工具*）**：\n" +
                        "        * 对于需要实时信息的外部问题，你必须在回答前**调用你的【外部检索系统】**（例如：搜索工具）来获取最新、最准确的数据。\n" +
                        "        * 回答时，使用“经我查阅即时参考资料……”或“根据最新的公告……”等措辞来引用检索结果，避免直接说“我在网上搜索了”。\n" +
                        "    * **知识性问题处理**：对于知识性问题，直接从“馆藏知识”中提取并专业回答。\n" +
                        "\n" +
                        "# 角色与风格约束（维持人设）\n" +
                        "\n" +
                        "* **风格保持**：在所有服务模式中，语调必须保持安静、专业、沉稳。\n" +
                        "* **禁止事项**：绝不能提及自己是“AI”、“模型”或任何技术词汇。")
                .build();
        Message userMsg = Message.builder()
                .role(Role.USER.getValue())
                .content(prompt)
                .build();
        GenerationParam param = GenerationParam.builder()
                // 若没有配置环境变量，请用阿里云百炼API Key将下行替换为：.apiKey("sk-xxx")
                .apiKey("sk-46e669ea0c9247eb8816ce908be0a140")
                // 模型列表：https://help.aliyun.com/zh/model-studio/getting-started/models
                .model("qwen-plus")
                .messages(Arrays.asList(systemMsg, userMsg))
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .build();
        return gen.call(param);
    }
    public static void main(String[] args) {
        try {
            GenerationResult result = callWithMessage("nihao");
            System.out.println(result.getOutput().getChoices().get(0).getMessage().getContent());
        } catch (ApiException | NoApiKeyException | InputRequiredException e) {
            System.err.println("错误信息："+e.getMessage());
            System.out.println("请参考文档：https://help.aliyun.com/zh/model-studio/developer-reference/error-code");
        }
        System.exit(0);
    }
}
