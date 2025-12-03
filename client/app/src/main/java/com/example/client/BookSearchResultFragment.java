package com.example.client;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class BookSearchResultFragment extends Fragment {

    private RecyclerView booksRecyclerView;
    private BookAdapter bookAdapter;
    private List<Book> bookList;
    private String searchQuery;
    private TextInputEditText searchInput;

    public static BookSearchResultFragment newInstance(String query) {
        BookSearchResultFragment fragment = new BookSearchResultFragment();
        Bundle args = new Bundle();
        args.putString("search_query", query);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            searchQuery = getArguments().getString("search_query");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_book_search_result, container, false);

        searchInput = view.findViewById(R.id.search_input);
        booksRecyclerView = view.findViewById(R.id.books_recycler_view);
        
        // 设置搜索框内容
        if (searchQuery != null) {
            searchInput.setText(searchQuery);
            searchInput.setSelection(searchQuery.length()); // 将光标移到末尾
        }
        
        bookList = new ArrayList<>();
        bookAdapter = new BookAdapter(bookList);
        booksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        booksRecyclerView.setAdapter(bookAdapter);

        // 添加书籍点击监听器
        bookAdapter.setOnBookClickListener(book -> {
            BookDetailFragment detailFragment = BookDetailFragment.newInstance(book);
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, detailFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        // 执行搜索
        performSearch();
        
        // 设置搜索框的回车键监听器
        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            performSearch();
            return true;
        });

        return view;
    }
    
    private void performSearch() {
        String query = searchInput.getText().toString().trim();
        // 这里应该调用实际的搜索API
        filterBooks(query);
    }

    private void filterBooks(String query) {
        if (query == null || query.isEmpty()) {
            bookAdapter.updateList(new ArrayList<>());
            return;
        }
        
        List<Book> filteredList = new ArrayList<>();
        List<Book> allBooks = getAllBooks(); // 获取所有图书
        
        for (Book book : allBooks) {
            if (book.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                    book.getAuthor().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(book);
            }
        }
        bookAdapter.updateList(filteredList);
    }
    
    private List<Book> getAllBooks() {
        // 在实际应用中，这里应该是从数据库或网络获取所有图书
        // 目前我们模拟一些数据
        List<Book> allBooks = new ArrayList<>();
        
        allBooks.add(new Book("Java核心技术", "Cay S. Horstmann", "机械工业出版社", "2020",
                "Java领域的经典权威著作，全面覆盖Java SE 9、10、11新特性，深入讲解面向对象编程、异常处理、泛型、集合框架等核心概念，是Java开发者必备的参考书。"));
        allBooks.add(new Book("Android开发艺术探索", "任玉刚", "电子工业出版社", "2019",
                "一线Android工程师的经典力作，深入剖析Android系统级开发的核心技术，涵盖四大组件工作原理、View体系、动画机制、Window机制等难点，帮助读者掌握Android开发精髓。"));
        allBooks.add(new Book("算法导论", "Thomas H. Cormen", "机械工业出版社", "2013",
                "算法领域的圣经级教材，系统全面地介绍了算法设计与分析的基本概念，涵盖排序、搜索、图算法、动态规划等经典算法，配有大量习题，适合计算机专业学生和研究人员。"));
        allBooks.add(new Book("设计模式", "Gang of Four", "机械工业出版社", "2010",
                "软件工程领域的里程碑式著作，介绍23种经典设计模式，帮助开发者编写可复用、可维护的面向对象软件，是每个程序员都应该掌握的设计原则和最佳实践。"));
        allBooks.add(new Book("重构", "Martin Fowler", "人民邮电出版社", "2019",
                "软件开发大师Martin Fowler的经典之作，详细介绍如何在不改变软件外部行为的前提下改善代码质量，提升系统可维护性，是提高代码质量和开发效率的必读书籍。"));

        allBooks.add(new Book("计算机网络", "谢希仁", "电子工业出版社", "2017",
                "国内最权威的计算机网络教材之一，系统介绍计算机网络的基本原理和关键技术，涵盖OSI模型、TCP/IP协议族、网络安全等内容，理论与实践相结合。"));
        allBooks.add(new Book("操作系统概念", "Abraham Silberschatz", "机械工业出版社", "2018",
                "操作系统领域的经典教材，全面介绍进程管理、内存管理、文件系统、分布式系统等核心概念，帮助读者深入理解操作系统的工作原理。"));
        allBooks.add(new Book("数据库系统概念", "Abraham Silberschatz", "机械工业出版社", "2019",
                "数据库领域的权威教材，系统阐述关系数据库理论、SQL语言、事务处理、并发控制等核心知识，理论与实践并重，适合数据库学习者和从业者。"));
        allBooks.add(new Book("编译原理", "Alfred V. Aho", "机械工业出版社", "2013",
                "编译器设计领域的经典教材，详细介绍词法分析、语法分析、语义分析、代码生成等编译过程，是理解和实现编程语言处理工具的重要参考资料。"));
        allBooks.add(new Book("计算机组成与设计", "David A. Patterson", "机械工业出版社", "2014",
                "计算机体系结构领域的权威教材，从数字逻辑电路到处理器设计，全面介绍计算机硬件系统的工作原理和设计方法。"));

        allBooks.add(new Book("软件工程", "Ian Sommerville", "机械工业出版社", "2016",
                "软件工程领域的标准教材，涵盖软件开发生命周期、需求工程、设计模式、测试策略等各个方面，帮助读者掌握现代软件工程的最佳实践。"));
        allBooks.add(new Book("人工智能:一种现代的方法", "Stuart Russell", "清华大学出版社", "2019",
                "AI领域的权威教材，系统介绍智能代理、问题求解、知识表示、机器学习、自然语言处理等AI核心主题，理论与应用并重。"));
        allBooks.add(new Book("机器学习", "周志华", "清华大学出版社", "2016",
                "被誉为\"西瓜书\"的机器学习经典教材，系统介绍监督学习、无监督学习、强化学习等各种机器学习方法，理论深入浅出，实例丰富。"));
        allBooks.add(new Book("深度学习", "Ian Goodfellow", "人民邮电出版社", "2017",
                "深度学习领域的权威教材，由该领域顶级专家撰写，全面介绍神经网络、卷积网络、循环网络、生成模型等深度学习核心技术。"));
        allBooks.add(new Book("Python编程:从入门到实践", "Eric Matthes", "人民邮电出版社", "2016",
                "广受欢迎的Python入门教材，通过实际项目引导读者学习Python编程，涵盖数据可视化、Web开发等应用领域，适合初学者快速上手。"));

        allBooks.add(new Book("代码大全", "Steve McConnell", "电子工业出版社", "2006",
                "软件构建领域的经典著作，全面介绍软件构造的最佳实践，涵盖变量命名、控制结构、代码布局、调试技术等，帮助程序员写出高质量代码。"));
        allBooks.add(new Book("程序员修炼之道", "Andrew Hunt", "电子工业出版社", "2011",
                "程序员必读的经典指南，介绍软件开发中的实用技巧和哲学思想，帮助程序员提升技能、改善工作流程，成为更专业的开发者。"));
        allBooks.add(new Book("人月神话", "Frederick P. Brooks", "清华大学出版社", "2015",
                "软件项目管理的经典著作，探讨软件开发的本质复杂性和项目管理的挑战，提出的\"人月\"概念对软件工程影响深远。"));
        allBooks.add(new Book("计算机程序的构造和解释", "Harold Abelson", "机械工业出版社", "2019",
                "MIT经典教材，使用Scheme语言讲授计算机科学核心概念，强调程序设计的思想和方法，培养计算思维能力。"));
        allBooks.add(new Book("黑客与画家", "Paul Graham", "人民邮电出版社", "2013",
                "著名程序员Paul Graham的随笔集，探讨编程语言设计、创业经验、技术趋势等话题，充满洞察力和启发性。"));

        allBooks.add(new Book("数学之美", "吴军", "人民邮电出版社", "2014",
                "揭示数学在信息技术中应用的美妙之处，涵盖自然语言处理、统计语言模型、信息检索等领域的数学原理，展现数学的力量。"));
        allBooks.add(new Book("浪潮之巅", "吴军", "人民邮电出版社", "2019",
                "全景式介绍IT产业发展的历史和规律，分析各大科技公司的兴衰成败，帮助读者理解技术变革背后的商业逻辑。"));
        allBooks.add(new Book("文明之光", "吴军", "人民邮电出版社", "2014",
                "从科技视角审视人类文明发展，探讨科学技术对社会进步的巨大推动作用，展现科技与人文的和谐统一。"));
        allBooks.add(new Book("硅谷之谜", "吴军", "人民邮电出版社", "2016",
                "深入解析硅谷成功的奥秘，分析创新文化、风险投资、人才聚集等因素，为科技创新提供启示。"));
        allBooks.add(new Book("大学之路", "吴军", "人民邮电出版社", "2015",
                "探讨世界一流大学的教育理念和人才培养模式，为学生和家长提供教育选择的参考和思考。"));

        allBooks.add(new Book("C++ Primer", "Stanley B. Lippman", "中国电力出版社", "2013",
                "C++学习的经典教材，全面介绍C++11标准的新特性，涵盖面向对象编程、模板、STL等核心内容，适合不同层次的C++学习者。"));
        allBooks.add(new Book("Effective Java", "Joshua Bloch", "机械工业出版社", "2019",
                "Java编程的最佳实践指南，提供78条具体的建议帮助开发者写出更加健壮、高效、可维护的Java代码。"));
        allBooks.add(new Book("Java并发编程实战", "Brian Goetz", "机械工业出版社", "2012",
                "深入讲解Java并发编程的核心概念和技术，涵盖线程安全、同步机制、线程池等主题，是Java并发开发的权威指南。"));
        allBooks.add(new Book("Spring实战", "Craig Walls", "人民邮电出版社", "2020",
                "Spring框架的实用指南，详细介绍Spring Core、Spring MVC、Spring Boot等技术，帮助开发者快速掌握企业级Java开发。"));
        allBooks.add(new Book("深入理解Java虚拟机", "周志明", "机械工业出版社", "2019",
                "深入剖析JVM工作机制，涵盖类加载机制、内存模型、垃圾回收、性能调优等高级主题，是Java高级开发者的必备参考书。"));

        allBooks.add(new Book("Head First Design Patterns", "Eric Freeman", "中国电力出版社", "2007",
                "以独特的方式介绍设计模式，通过大量图表、练习和幽默元素帮助读者轻松掌握23种经典设计模式及其应用场景。"));
        allBooks.add(new Book("JavaScript高级程序设计", "Nicholas C. Zakas", "人民邮电出版社", "2012",
                "JavaScript学习的权威教材，全面介绍JavaScript语言核心、DOM编程、事件处理、Ajax等Web开发技术，适合前端开发者深入学习。"));
        allBooks.add(new Book("你不知道的JavaScript", "Kyle Simpson", "人民邮电出版社", "2015",
                "深入挖掘JavaScript语言的核心机制和工作原理，帮助开发者真正理解JavaScript的作用域、闭包、this绑定等重要概念。"));
        allBooks.add(new Book("CSS权威指南", "Eric A. Meyer", "中国电力出版社", "2019",
                "CSS领域的权威参考书，详细介绍CSS选择器、盒模型、定位、浮动等核心概念，帮助开发者掌握网页样式设计的精髓。"));
        allBooks.add(new Book("HTML5权威指南", "Adam Freeman", "人民邮电出版社", "2014",
                "全面介绍HTML5新特性的实用指南，涵盖语义化标签、表单增强、多媒体支持、本地存储等技术，是Web开发者的重要参考。"));

        allBooks.add(new Book("Node.js开发指南", "郭家宝", "人民邮电出版社", "2012",
                "Node.js入门与实践的经典教材，介绍服务器端JavaScript开发的基础知识和实际应用，帮助开发者快速掌握全栈开发技能。"));
        allBooks.add(new Book("React学习手册", "Alex Banks", "人民邮电出版社", "2017",
                "React框架的学习指南，详细介绍组件化开发、状态管理、路由配置等核心技术，通过实例帮助读者掌握现代前端开发。"));
        allBooks.add(new Book("Vue.js实战", "梁灏", "清华大学出版社", "2017",
                "Vue.js框架的实践指南，从基础概念到项目实战，全面介绍Vue的核心特性和生态系统，适合前端开发者学习和应用。"));
        allBooks.add(new Book("Angular权威教程", "Lerner, Ari", "人民邮电出版社", "2017",
                "Angular框架的权威教程，系统介绍TypeScript、组件、依赖注入、路由等Angular核心技术，帮助开发者构建复杂的单页应用。"));
        allBooks.add(new Book("Webpack实战", "居玉皓", "机械工业出版社", "2022",
                "前端构建工具Webpack的实战指南，详细介绍模块打包、代码分割、性能优化等技术，帮助开发者提升前端工程化水平。"));

        allBooks.add(new Book("Docker技术入门与实战", "杨保华", "机械工业出版社", "2018",
                "容器技术Docker的入门与实践指南，介绍镜像制作、容器管理、网络配置、数据持久化等核心技术，帮助开发者快速掌握容器化部署。"));
        allBooks.add(new Book("Kubernetes权威指南", "龚正", "电子工业出版社", "2019",
                "容器编排平台Kubernetes的权威指南，全面介绍Pod、Service、Deployment等核心概念，帮助读者掌握容器集群管理技术。"));
        allBooks.add(new Book("微服务架构设计模式", "Chris Richardson", "机械工业出版社", "2019",
                "微服务架构的设计模式和实践指南，介绍服务拆分、数据管理、服务发现、容错处理等关键技术，帮助开发者构建可靠的分布式系统。"));
        allBooks.add(new Book("领域驱动设计", "Eric Evans", "人民邮电出版社", "2018",
                "软件设计方法学的经典著作，介绍如何通过领域建模解决复杂业务问题，提出实体、值对象、聚合根等核心概念，是复杂系统设计的指导原则。"));
        allBooks.add(new Book("敏捷软件开发", "Robert C. Martin", "清华大学出版社", "2003",
                "敏捷开发方法的经典教材，介绍敏捷宣言的原则和实践，涵盖Scrum、极限编程等具体方法，帮助团队提高软件开发效率。"));

        allBooks.add(new Book("测试驱动开发", "Kent Beck", "中国电力出版社", "2004",
                "测试驱动开发(TDD)的经典著作，详细介绍红绿重构的开发循环，帮助开发者通过测试先行的方式提高代码质量和设计水平。"));
        allBooks.add(new Book("持续交付", "Jez Humble", "人民邮电出版社", "2011",
                "软件交付领域的权威指南，介绍如何通过自动化构建、测试、部署实现快速可靠的软件发布，提升软件交付效率和质量。"));
        allBooks.add(new Book("DevOps实践指南", "Gene Kim", "人民邮电出版社", "2017",
                "DevOps理念和实践的全面指南，介绍如何打破开发与运维之间的壁垒，通过文化和技术手段实现高效的软件交付。"));
        allBooks.add(new Book("Linux命令行与shell脚本编程大全", "Richard Blum", "人民邮电出版社", "2016",
                "Linux系统管理和Shell编程的权威指南，详细介绍常用命令、文本处理、进程管理、脚本编写等技能，是Linux用户的必备参考。"));
        allBooks.add(new Book("鸟哥的Linux私房菜", "鸟哥", "人民邮电出版社", "2017",
                "最受欢迎的Linux入门教材之一，从基础概念到系统管理，循序渐进地介绍Linux系统的使用和管理技巧。"));

        allBooks.add(new Book("UNIX环境高级编程", "W. Richard Stevens", "人民邮电出版社", "2014",
                "UNIX系统编程的经典教材，详细介绍进程控制、信号处理、文件I/O、网络编程等系统级编程技术，是UNIX/Linux开发者的重要参考。"));
        allBooks.add(new Book("TCP/IP详解 卷1", "W. Richard Stevens", "机械工业出版社", "2016",
                "网络协议领域的权威著作，深入剖析TCP/IP协议族的工作原理和实现细节，是网络程序员和系统管理员的必备参考书。"));
        allBooks.add(new Book("图解TCP/IP", "竹下隆史", "人民邮电出版社", "2012",
                "以图文并茂的方式介绍TCP/IP协议，通过大量图解帮助读者直观理解网络通信原理，适合网络技术初学者入门。"));
        allBooks.add(new Book("网络安全基础", "William Stallings", "清华大学出版社", "2017",
                "网络安全领域的基础教材，介绍加密算法、认证机制、防火墙、入侵检测等安全技术，帮助读者建立完整的安全知识体系。"));
        allBooks.add(new Book("加密与解密", "看雪学院", "电子工业出版社", "2015",
                "信息安全技术的实践指南，详细介绍密码学原理、加密算法实现、逆向工程等技术，是安全研究人员的重要参考资料。"));

        allBooks.add(new Book("游戏设计艺术", "Jesse Schell", "电子工业出版社", "2015",
                "游戏设计领域的权威教材，系统介绍游戏设计的基本原理和方法，涵盖玩家心理学、游戏机制、叙事设计等核心主题。"));
        allBooks.add(new Book("Unity 3D游戏开发", "宣雨松", "人民邮电出版社", "2018",
                "Unity游戏引擎的实用教程，从基础操作到高级技巧，详细介绍3D游戏开发的各个环节，帮助开发者快速掌握游戏制作技能。"));
        allBooks.add(new Book("虚幻引擎4从入门到精通", "罗金海", "清华大学出版社", "2019",
                "虚幻引擎4的学习指南，介绍材质系统、蓝图编程、动画系统等核心技术，通过实例教学帮助读者掌握专业级游戏开发。"));
        allBooks.add(new Book("移动应用UI设计模式", "Theresa Neil", "人民邮电出版社", "2014",
                "移动应用界面设计的模式库，介绍各种UI组件和交互模式的最佳实践，帮助设计师创建直观易用的移动应用界面。"));
        allBooks.add(new Book("写给大家看的设计书", "Robin Williams", "人民邮电出版社", "2015",
                "设计入门的经典教材，介绍对比、重复、对齐、亲密性四大设计原则，帮助非设计专业人员提升视觉设计能力。"));
        
        return allBooks;
    }
}