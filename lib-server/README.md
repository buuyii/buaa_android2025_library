# Library Server

Django REST Framework 服务端

## 启动服务

```bash
cd lib-server/library_server
python manage.py runserver 0.0.0.0:8000
```

## API URL 配置

URL 配置文件位于: `library_server/library_server/urls.py`

## 可用的 API 接口

| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/books/` | GET, POST | 获取图书列表或添加新图书 |
| `/api/books/{id}/` | GET, PUT, DELETE | 获取、更新或删除单本图书 |
| `/api/borrow_records/` | GET, POST | 获取借阅记录列表或创建借阅记录 |
| `/api/borrow_records/{id}/` | GET, PUT, DELETE | 获取、更新或删除单条借阅记录 |
| `/api/borrow_records/{id}/borrow/` | POST | 借书操作 |
| `/api/borrow_records/{id}/return_book/` | POST | 还书操作 |
| `/admin/` | GET | Django 管理后台 |

## 在 Android 客户端中使用

Android 客户端需要配置服务器地址连接这些 API。基础 URL 格式:

```
http://<服务器IP>:8000/api/
```

示例:
- 本地开发 (模拟器): `http://10.0.2.2:8000/api/`
- 本地开发 (真机): `http://<电脑局域网IP>:8000/api/`
- 生产环境: `http://your-domain.com:8000/api/`
