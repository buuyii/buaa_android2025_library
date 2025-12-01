// CategoryBrowseFragment.java
package com.example.client;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CategoryBrowseFragment extends Fragment {

    private ExpandableListView expandableListView;
    private CategoryExpandableListAdapter adapter;
    private List<String> categoryList;
    private HashMap<String, List<Book>> categoryBooksMap;

    public CategoryBrowseFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category_browse, container, false);
        
        expandableListView = view.findViewById(R.id.category_expandable_list);
        
        initData();
        
        adapter = new CategoryExpandableListAdapter(getContext(), categoryList, categoryBooksMap);
        expandableListView.setAdapter(adapter);
        
        // Set click listener for child items (books)
        expandableListView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
            String category = categoryList.get(groupPosition);
            Book book = categoryBooksMap.get(category).get(childPosition);
            
            BookDetailFragment detailFragment = BookDetailFragment.newInstance(book);
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, detailFragment)
                        .addToBackStack(null)
                        .commit();
            }
            return true;
        });
        
        return view;
    }
    
    private void initData() {
        categoryList = new ArrayList<>();
        categoryBooksMap = new HashMap<>();
        
        // Add categories
        categoryList.add("编程语言");
        categoryList.add("计算机科学");
        categoryList.add("软件工程");
        categoryList.add("网络安全");
        categoryList.add("人工智能");
        categoryList.add("游戏开发");
        categoryList.add("操作系统");
        categoryList.add("数据库");
        categoryList.add("移动开发");
        categoryList.add("网络通信");
        
        // Add books to categories (in a real app, this would come from a database)
        List<Book> programmingBooks = new ArrayList<>();
        programmingBooks.add(new Book("Java核心技术", "Cay S. Horstmann", "机械工业出版社", "2020",
                "Java领域的经典权威著作，全面覆盖Java SE 9、10、11新特性，深入讲解面向对象编程、异常处理、泛型、集合框架等核心概念，是Java开发者必备的参考书。"));
        programmingBooks.add(new Book("C++ Primer", "Stanley B. Lippman", "中国电力出版社", "2013",
                "C++学习的经典教材，全面介绍C++11标准的新特性，涵盖面向对象编程、模板、STL等核心内容，适合不同层次的C++学习者。"));
        programmingBooks.add(new Book("Python编程:从入门到实践", "Eric Matthes", "人民邮电出版社", "2016",
                "广受欢迎的Python入门教材，通过实际项目引导读者学习Python编程，涵盖数据可视化、Web开发等应用领域，适合初学者快速上手。"));
        programmingBooks.add(new Book("JavaScript高级程序设计", "Nicholas C. Zakas", "人民邮电出版社", "2019",
                "JavaScript学习的权威教材，全面介绍JavaScript语言核心、DOM编程、事件处理、Ajax等Web开发技术，适合前端开发者深入学习。"));
        
        List<Book> computerScienceBooks = new ArrayList<>();
        computerScienceBooks.add(new Book("算法导论", "Thomas H. Cormen", "机械工业出版社", "2013",
                "算法领域的圣经级教材，系统全面地介绍了算法设计与分析的基本概念，涵盖排序、搜索、图算法、动态规划等经典算法，配有大量习题，适合计算机专业学生和研究人员。"));
        computerScienceBooks.add(new Book("计算机网络", "谢希仁", "电子工业出版社", "2017",
                "国内最权威的计算机网络教材之一，系统介绍计算机网络的基本原理和关键技术，涵盖OSI模型、TCP/IP协议族、网络安全等内容，理论与实践相结合。"));
        computerScienceBooks.add(new Book("操作系统概念", "Abraham Silberschatz", "机械工业出版社", "2018",
                "操作系统领域的经典教材，全面介绍进程管理、内存管理、文件系统、分布式系统等核心概念，帮助读者深入理解操作系统的工作原理。"));
        computerScienceBooks.add(new Book("计算机组成与设计", "David A. Patterson", "机械工业出版社", "2014",
                "计算机体系结构领域的权威教材，从数字逻辑电路到处理器设计，全面介绍计算机硬件系统的工作原理和设计方法。"));
        
        List<Book> softwareEngineeringBooks = new ArrayList<>();
        softwareEngineeringBooks.add(new Book("设计模式", "Gang of Four", "机械工业出版社", "2010",
                "软件工程领域的里程碑式著作，介绍23种经典设计模式，帮助开发者编写可复用、可维护的面向对象软件，是每个程序员都应该掌握的设计原则和最佳实践。"));
        softwareEngineeringBooks.add(new Book("重构", "Martin Fowler", "人民邮电出版社", "2019",
                "软件开发大师Martin Fowler的经典之作，详细介绍如何在不改变软件外部行为的前提下改善代码质量，提升系统可维护性，是提高代码质量和开发效率的必读书籍。"));
        softwareEngineeringBooks.add(new Book("代码大全", "Steve McConnell", "电子工业出版社", "2006",
                "软件构建领域的经典著作，全面介绍软件构造的最佳实践，涵盖变量命名、控制结构、代码布局、调试技术等，帮助程序员写出高质量代码。"));
        softwareEngineeringBooks.add(new Book("人月神话", "Frederick P. Brooks", "清华大学出版社", "2015",
                "软件项目管理的经典著作，探讨软件开发的本质复杂性和项目管理的挑战，提出的\"人月\"概念对软件工程影响深远。"));
        
        // Add more books to other categories...
        List<Book> aiBooks = new ArrayList<>();
        aiBooks.add(new Book("人工智能:一种现代的方法", "Stuart Russell", "清华大学出版社", "2019",
                "AI领域的权威教材，系统介绍智能代理、问题求解、知识表示、机器学习、自然语言处理等AI核心主题，理论与应用并重。"));
        aiBooks.add(new Book("机器学习", "周志华", "清华大学出版社", "2016",
                "被誉为\"西瓜书\"的机器学习经典教材，系统介绍监督学习、无监督学习、强化学习等各种机器学习方法，理论深入浅出，实例丰富。"));
        aiBooks.add(new Book("深度学习", "Ian Goodfellow", "人民邮电出版社", "2017",
                "深度学习领域的权威教材，由该领域顶级专家撰写，全面介绍神经网络、卷积网络、循环网络、生成模型等深度学习核心技术。"));
        
        List<Book> mobileDevBooks = new ArrayList<>();
        mobileDevBooks.add(new Book("Android开发艺术探索", "任玉刚", "电子工业出版社", "2019",
                "一线Android工程师的经典力作，深入剖析Android系统级开发的核心技术，涵盖四大组件工作原理、View体系、动画机制、Window机制等难点，帮助读者掌握Android开发精髓。"));
        
        // Put books into map
        categoryBooksMap.put("编程语言", programmingBooks);
        categoryBooksMap.put("计算机科学", computerScienceBooks);
        categoryBooksMap.put("软件工程", softwareEngineeringBooks);
        categoryBooksMap.put("人工智能", aiBooks);
        categoryBooksMap.put("移动开发", mobileDevBooks);
        // Add empty lists for other categories
        for (String category : categoryList) {
            if (!categoryBooksMap.containsKey(category)) {
                categoryBooksMap.put(category, new ArrayList<>());
            }
        }
    }
}