## RBAC

Hệ thống phân quyền 2 tầng: **Global** và **Organization**.

```
GLOBAL LAYER
  User ──M:N──> UserGlobalRole ──M:1──> GlobalRole ──M:N──> GlobalPermission

ORGANIZATION LAYER
  CreatorProfile ──M:1──> OrgRole ──M:N──> OrgPermission
```

### Global Roles

| Role          | is_system | Auto-assign                 |
|---------------|-----------|-----------------------------|
| `user`        | true      | Khi đăng ký                 |
| `creator`     | true      | Khi CreatorProfile được tạo |
| `moderator`   | true      | Manual                      |
| `admin`       | true      | Manual                      |
| `super_admin` | true      | Manual                      |

### Global Permissions

| Permission                | Category     | Mô tả                                           |
|---------------------------|--------------|-------------------------------------------------|
| Permission                | Category     | Mô tả                                           |
| ------------------------- | ------------ | ----------------------------------------------- |
| `profile:edit_own`        | `profile`    | Chỉnh sửa profile của mình                      |
| `comment:create`          | `comment`    | Đăng comment                                    |
| `comment:edit_own`        | `comment`    | Chỉnh sửa comment của mình                      |
| `comment:delete_own`      | `comment`    | Xóa comment của mình                            |
| `comment:report`          | `comment`    | Báo cáo comment vi phạm                         |
| `comment:delete_any`      | `comment`    | Xóa bất kỳ comment (moderator/admin)            |
| `bookmark:manage`         | `bookmark`   | Thêm / xóa bookmark                             |
| `follow:manage`           | `follow`     | Follow / unfollow user hoặc organization        |
| `rating:manage`           | `rating`     | Đánh giá nội dung (thêm / sửa / xóa rating)     |
| `user:view`               | `user`       | Xem thông tin user                              |
| `user:ban`                | `user`       | Khóa tài khoản user                             |
| `user:unban`              | `user`       | Mở khóa tài khoản                               |
| `creator:view_pending`    | `creator`    | Xem danh sách creator chờ duyệt                 |
| `creator:approve`         | `creator`    | Duyệt creator                                   |
| `creator:reject`          | `creator`    | Từ chối creator                                 |
| `creator:suspend`         | `creator`    | Khóa creator                                    |
| `content:create`          | `content`    | Tạo nội dung mới (chỉ của mình)                 |
| `content:edit_own`        | `content`    | Chỉnh sửa nội dung của mình                     |
| `content:delete_own`      | `content`    | Xóa nội dung của mình                           |
| `content:publish_own`     | `content`    | Publish / unpublish nội dung của mình           |
| `content:report`          | `content`    | Báo cáo nội dung vi phạm                        |
| `content:view_reports`    | `content`    | Xem reports vi phạm                             |
| `content:moderate`        | `content`    | Xử lý reports vi phạm                           |
| `content:delete_any`      | `content`    | Xóa bất kỳ nội dung (admin/moderator)           |
| `system:settings`         | `system`     | Quản lý settings hệ thống                       |
| `system:roles`            | `system`     | Quản lý roles & permissions                     |

> **`_own` permissions:** Service layer chịu trách nhiệm kiểm tra ownership —
> khi user có `comment:edit_own`, code phải xác nhận comment thuộc về họ trước khi cho phép.

### Global Permissions Matrix

| Role          | Permissions                                                                                                                                                                               |
|---------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `super_admin` | `*`                                                                                                                                                                                       |
| `admin`       | `user:*`, `creator:*`, `content:*`, `comment:*`, `profile:*`                                                                                                                              |
| `moderator`   | `content:view_reports`, `content:moderate`, `content:delete_any`, `comment:delete_any`                                                                                                    |
| `creator`     | `content:create`, `content:edit_own`, `content:delete_own`, `content:publish_own`, `content:report`, `comment:*`, `profile:edit_own`, `bookmark:manage`, `follow:manage`, `rating:manage` |
| `user`        | `comment:create`, `comment:edit_own`, `comment:delete_own`, `comment:report`, `profile:edit_own`, `bookmark:manage`, `follow:manage`, `rating:manage`, `content:report`                   |

### Org System Roles

| Role         | Level |
|--------------|-------|
| `owner`      | 100   |
| `admin`      | 80    |
| `editor`     | 60    |
| `translator` | 40    |
| `member`     | 20    |

### Org Permissions Matrix

| Role         | Permissions                                                         |
|--------------|---------------------------------------------------------------------|
| `owner`      | `*`                                                                 |
| `admin`      | `org:update`, `member:*`, `request:*`, `content:*`, `translation:*` |
| `editor`     | `content:*`, `translation:*`                                        |
| `translator` | `translation:create`, `translation:edit`                            |
| `member`     | `member:view`                                                       |
