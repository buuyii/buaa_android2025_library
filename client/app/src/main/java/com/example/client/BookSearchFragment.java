package com.example.client;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class BookSearchFragment extends Fragment {

    private EditText searchInput;
    
    // ViewPager2 和 TabLayout 组件
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    
    // 推荐板块数据
    private List<Book> hotBooks;
    private List<Book> newBooks;
    private List<Book> recommendedBooks;
    
    private ImageButton searchHistoryButton;
    private ImageButton favoriteBooksButton;
    private ImageButton bookCategoriesButton;

    public BookSearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_book_search, container, false);

        searchInput = view.findViewById(R.id.search_input);
        
        // 初始化 ViewPager2 和 TabLayout
        viewPager = view.findViewById(R.id.view_pager);
        tabLayout = view.findViewById(R.id.tab_layout);
        
        searchHistoryButton = view.findViewById(R.id.search_history_button);
        favoriteBooksButton = view.findViewById(R.id.favorite_books_button);
        bookCategoriesButton = view.findViewById(R.id.book_categories_button);

        // 设置输入框的回车键监听器
        searchInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                performSearch();
                return true;
            }
        });

        // 设置输入框点击监听器，点击后跳转到搜索结果页面
        searchInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jumpToSearchResult("");
            }
        });

        // 设置模块按钮的点击监听器
        searchHistoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 显示搜索记录
                showSearchHistory();
            }
        });

        favoriteBooksButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 显示收藏图书
                showFavoriteBooks();
            }
        });

        bookCategoriesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 按分类浏览图书
                showCategoryBrowse();
            }
        });

        // 初始化推荐图书数据
        initRecommendedBooks();
        
        // 设置 ViewPager2 适配器
        setupViewPager();

        return view;
    }
    
    private void setupViewPager() {
        // 创建包含三个推荐板块的 Fragment 列表
        ArrayList<Fragment> fragments = new ArrayList<>();
        fragments.add(BookRecommendFragment.newInstance(hotBooks));
        fragments.add(BookRecommendFragment.newInstance(newBooks));
        fragments.add(BookRecommendFragment.newInstance(recommendedBooks));
        
        // 创建 ViewPager2 适配器
        BookPagerAdapter pagerAdapter = new BookPagerAdapter(this);
        pagerAdapter.setFragments(fragments);
        
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOffscreenPageLimit(3); // 预加载所有页面
        
        // 使用 TabLayoutMediator 将 TabLayout 和 ViewPager2 关联起来
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("热门图书");
                            break;
                        case 1:
                            tab.setText("新书速递");
                            break;
                        case 2:
                            tab.setText("馆藏推荐");
                            break;
                    }
                }
        ).attach();
    }
    
    private void showSearchHistory() {
        // 获取最新的搜索历史记录
        List<Book> searchHistory = SearchHistoryManager.getInstance().getSearchHistory();
        
        BookListFragment searchHistoryFragment = BookListFragment.newInstance("搜索历史", searchHistory);
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, searchHistoryFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }
    
    private void showFavoriteBooks() {
        // 获取最新的收藏图书列表
        List<Book> favoriteBooks = FavoritesManager.getInstance().getFavoriteBooks();
        
        BookListFragment favoriteBooksFragment = BookListFragment.newInstance("收藏图书", favoriteBooks);
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, favoriteBooksFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }
    
    private void showCategoryBrowse() {
        CategoryBrowseFragment categoryBrowseFragment = new CategoryBrowseFragment();
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, categoryBrowseFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }
    
    private void performSearch() {
        String query = searchInput.getText().toString().trim();
        if (!query.isEmpty()) {
            // 跳转到搜索结果页面
            jumpToSearchResult(query);
        }
    }

    private void jumpToSearchResult(String query) {
        BookSearchResultFragment searchResultFragment = BookSearchResultFragment.newInstance(query);
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, searchResultFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void initRecommendedBooks() {
        // 创建热门图书列表（10本书）
        hotBooks = new ArrayList<>();
        hotBooks.add(new Book("Java核心技术", "Cay S. Horstmann", "机械工业出版社", "2020", 
                "Java领域的经典权威著作，全面覆盖Java SE 9、10、11新特性，深入讲解面向对象编程、异常处理、泛型、集合框架等核心概念，是Java开发者必备的参考书。", R.drawable.javacore_technology));
        hotBooks.add(new Book("算法导论", "Thomas H. Cormen", "机械工业出版社", "2013", 
                "算法领域的圣经级教材，系统全面地介绍了算法设计与分析的基本概念，涵盖排序、搜索、图算法、动态规划等经典算法，配有大量习题，适合计算机专业学生和研究人员。", R.drawable.algorithm));
        hotBooks.add(new Book("设计模式", "Gang of Four", "机械工业出版社", "2010", 
                "软件工程领域的里程碑式著作，介绍23种经典设计模式，帮助开发者编写可复用、可维护的面向对象软件，是每个程序员都应该掌握的设计原则和最佳实践。", R.drawable.designpatterns));
        hotBooks.add(new Book("重构", "Martin Fowler", "人民邮电出版社", "2019", 
                "软件开发大师Martin Fowler的经典之作，详细介绍如何在不改变软件外部行为的前提下改善代码质量，提升系统可维护性，是提高代码质量和开发效率的必读书籍。", R.drawable.rebuild));
        hotBooks.add(new Book("代码大全", "Steve McConnell", "电子工业出版社", "2006", 
                "软件构建领域的经典著作，全面介绍软件构造的最佳实践，涵盖变量命名、控制结构、代码布局、调试技术等，帮助程序员写出高质量代码。", R.drawable.codecomplete));
        hotBooks.add(new Book("计算机网络", "谢希仁", "电子工业出版社", "2017",
                "国内最权威的计算机网络教材之一，系统介绍计算机网络的基本原理和关键技术，涵盖OSI模型、TCP/IP协议族、网络安全等内容，理论与实践相结合。", R.drawable.jsjnetwork));
        hotBooks.add(new Book("操作系统概念", "Abraham Silberschatz", "机械工业出版社", "2018",
                "操作系统领域的经典教材，全面介绍进程管理、内存管理、文件系统、分布式系统等核心概念，帮助读者深入理解操作系统的工作原理。", R.drawable.os));
        hotBooks.add(new Book("数据库系统概念", "Abraham Silberschatz", "机械工业出版社", "2019",
                "数据库领域的权威教材，系统阐述关系数据库理论、SQL语言、事务处理、并发控制等核心知识，理论与实践并重，适合数据库学习者和从业者。", R.drawable.database));
        hotBooks.add(new Book("编译原理", "Alfred V. Aho", "人民邮电出版社", "2013",
                "编译器设计领域的经典教材，详细介绍词法分析、语法分析、语义分析、代码生成等编译过程，是理解和实现编程语言处理工具的重要参考资料。", R.drawable.compile));
        hotBooks.add(new Book("计算机组成与设计", "David A. Patterson", "机械工业出版社", "2014",
                "计算机体系结构领域的权威教材，从数字逻辑电路到处理器设计，全面介绍计算机硬件系统的工作原理和设计方法。", R.drawable.co));

        // 创建新书列表（10本书）
        newBooks = new ArrayList<>();
        newBooks.add(new Book("深入理解Java虚拟机", "周志明", "机械工业出版社", "2019", 
                "深入剖析JVM工作机制，涵盖类加载机制、内存模型、垃圾回收、性能调优等高级主题，是Java高级开发者的必备参考书。", R.drawable.javaxuniji));
        newBooks.add(new Book("微服务架构设计模式", "Chris Richardson", "机械工业出版社", "2019", 
                "微服务架构的设计模式和实践指南，介绍服务拆分、数据管理、服务发现、容错处理等关键技术，帮助开发者构建可靠的分布式系统。", R.drawable.microservices));
        newBooks.add(new Book("Kubernetes权威指南", "龚正", "电子工业出版社", "2019", 
                "容器编排平台Kubernetes的权威指南，全面介绍Pod、Service、Deployment等核心概念，帮助读者掌握容器集群管理技术。", R.drawable.kubernetes));
        newBooks.add(new Book("Spring实战", "Craig Walls", "人民邮电出版社", "2019", 
                "Spring框架的实用指南，详细介绍Spring Core、Spring MVC、Spring Boot等技术，帮助开发者快速掌握企业级Java开发。", R.drawable.spring));
        newBooks.add(new Book("Effective Java", "Joshua Bloch", "机械工业出版社", "2019", 
                "Java编程的最佳实践指南，提供78条具体的建议帮助开发者写出更加健壮、高效、可维护的Java代码。", R.drawable.effectivejava));
        newBooks.add(new Book("Android开发艺术探索", "任玉刚", "电子工业出版社", "2019",
                "一线Android工程师的经典力作，深入剖析Android系统级开发的核心技术，涵盖四大组件工作原理、View体系、动画机制、Window机制等难点，帮助读者掌握Android开发精髓。", R.drawable.android));
        newBooks.add(new Book("机器学习", "周志华", "清华大学出版社", "2016",
                "被誉为\"西瓜书\"的机器学习经典教材，系统介绍监督学习、无监督学习、强化学习等各种机器学习方法，理论深入浅出，实例丰富。", R.drawable.machinelearning));
        newBooks.add(new Book("深度学习", "Ian Goodfellow", "人民邮电出版社", "2017",
                "深度学习领域的权威教材，由该领域顶级专家撰写，全面介绍神经网络、卷积网络、循环网络、生成模型等深度学习核心技术。", R.drawable.deeplearning));
        newBooks.add(new Book("Python编程:从入门到实践", "Eric Matthes", "人民邮电出版社", "2016",
                "广受欢迎的Python入门教材，通过实际项目引导读者学习Python编程，涵盖数据可视化、Web开发等应用领域，适合初学者快速上手。", R.drawable.py));
        newBooks.add(new Book("软件工程", "Ian Sommerville", "机械工业出版社", "2016",
                "软件工程领域的标准教材，涵盖软件开发生命周期、需求工程、设计模式、测试策略等各个方面，帮助读者掌握现代软件工程的最佳实践。", R.drawable.se));

        // 创建推荐图书列表（10本书）
        recommendedBooks = new ArrayList<>();
        recommendedBooks.add(new Book("程序员修炼之道", "Andrew Hunt", "电子工业出版社", "2011", 
                "程序员必读的经典指南，介绍软件开发中的实用技巧和哲学思想，帮助程序员提升技能、改善工作流程，成为更专业的开发者。", R.drawable.thewayoftheprogrammer));
        recommendedBooks.add(new Book("人月神话", "Frederick P. Brooks", "清华大学出版社", "2015", 
                "软件项目管理的经典著作，探讨软件开发的本质复杂性和项目管理的挑战，提出的\"人月\"概念对软件工程影响深远。", R.drawable.themythicalmanmonth));
        recommendedBooks.add(new Book("计算机程序的构造和解释", "Harold Abelson", "机械工业出版社", "2015", 
                "MIT经典教材，使用Scheme语言讲授计算机科学核心概念，强调程序设计的思想和方法，培养计算思维能力。", R.drawable.structureandinterpretation));
        recommendedBooks.add(new Book("黑客与画家", "Paul Graham", "人民邮电出版社", "2013", 
                "著名程序员Paul Graham的随笔集，探讨编程语言设计、创业经验、技术趋势等话题，充满洞察力和启发性。", R.drawable.hackersandpainters));
        recommendedBooks.add(new Book("数学之美", "吴军", "人民邮电出版社", "2014", 
                "揭示数学在信息技术中应用的美妙之处，涵盖自然语言处理、统计语言模型、信息检索等领域的数学原理，展现数学的力量。", R.drawable.math));
        recommendedBooks.add(new Book("人工智能:一种现代的方法", "Stuart Russell", "清华大学出版社", "2019",
                "AI领域的权威教材，系统介绍智能代理、问题求解、知识表示、机器学习、自然语言处理等AI核心主题，理论与应用并重。", R.drawable.ai));
        recommendedBooks.add(new Book("你不知道的JavaScript", "Kyle Simpson", "人民邮电出版社", "2015",
                "深入挖掘JavaScript语言的核心机制和工作原理，帮助开发者真正理解JavaScript的作用域、闭包、this绑定等重要概念。", R.drawable.unknownjs));
        recommendedBooks.add(new Book("Vue.js实战", "梁灏", "电子工业出版社", "2019",
                "Vue.js框架的实践指南，从基础概念到项目实战，全面介绍Vue的核心特性和生态系统，适合前端开发者学习和应用。", R.drawable.vuejs));
        recommendedBooks.add(new Book("Docker技术入门与实战", "杨保华", "机械工业出版社", "2018",
                "容器技术Docker的入门与实践指南，介绍镜像制作、容器管理、网络配置、数据持久化等核心技术，帮助开发者快速掌握容器化部署。", R.drawable.docker));
        recommendedBooks.add(new Book("Linux命令行与shell脚本编程大全", "Richard Blum", "人民邮电出版社", "2016",
                "Linux系统管理和Shell编程的权威指南，详细介绍常用命令、文本处理、进程管理、脚本编写等技能，是Linux用户的必备参考。", R.drawable.linuxandshell));
    }
}