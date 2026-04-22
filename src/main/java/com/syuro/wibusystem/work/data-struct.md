# Work — Data Structure & Rules

## Tổng quan

`Work` là aggregate root của toàn bộ hệ thống nội dung. Mọi thứ từ metadata đến nội dung dịch thuật đều bắt đầu từ đây.

---

## Cây cấu trúc dữ liệu

```
Work
├── [metadata]
│   ├── WorkLocalization        (tên, synopsis theo ngôn ngữ)
│   ├── WorkGenre               (thể loại)
│   ├── WorkStaff               (tác giả, hoạ sĩ, đạo diễn...)
│   ├── WorkOrganization        (studio, nhà xuất bản...)
│   └── WorkCharacter
│       ├── role: MAIN / SUPPORTING / BACKGROUND
│       └── WorkCharacterDub    (seiyuu lồng tiếng theo ngôn ngữ)
│
├── [content — MANGA]
│   └── MangaVolume
│       └── MangaChapter
│           ├── MangaChapterPage            (trang gốc)
│           └── MangaChapterLocalization
│               └── MangaChapterLocalizationPage  (trang đã dịch/typeset)
│
├── [content — NOVEL]
│   └── NovelVolume
│       └── NovelChapter
│           └── NovelChapterLocalization    (nội dung rich text đã dịch)
│
└── [content — ANIME]
    └── AnimeSeason
        ├── AnimeEpisode
        └── AnimeEpisodeStream              (link stream ngoài)
```

---

## Work

| Field              | Ý nghĩa                                                                            |
|--------------------|------------------------------------------------------------------------------------|
| `type`             | `MANGA` / `NOVEL` / `ANIME` — xác định nhánh content sẽ dùng                       |
| `status`           | `ON_GOING` / `COMPLETED` / `PENDING` / `CANCELLED`                                 |
| `originalLanguage` | Ngôn ngữ gốc của tác phẩm (mặc định `"jp"`)                                        |
| `isOneshot`        | `true` = tác phẩm 1 tập/1 mùa (xem quy tắc Oneshot bên dưới)                       |
| `orgId`            | ID org từ identity schema — nullable, có giá trị khi work thuộc sở hữu của tổ chức |
| `creatorProfileId` | Creator tạo work — luôn có, kể cả khi thuộc org                                    |
| `airedFrom/To`     | Ngày phát sóng / kết thúc. `airedTo = null` khi đang `ON_GOING`                    |
| `ageRating`        | `G / PG / PG13 / R17 / R`                                                          |

---

## Quy tắc Oneshot

### Khi `isOneshot = true`

Service layer **tự động tạo** 1 record ẩn khi Work được tạo:

| WorkType | Entity tự tạo | Giá trị                          |
|----------|---------------|----------------------------------|
| MANGA    | `MangaVolume` | `volumeNumber=1, isVirtual=true` |
| NOVEL    | `NovelVolume` | `volumeNumber=1, isVirtual=true` |
| ANIME    | `AnimeSeason` | `seasonNumber=1, isVirtual=true` |

- `isVirtual=true` → **không hiển thị** layer Volume/Season cho user
- API trả về chapter/episode list trực tiếp, bỏ qua tầng Volume/Season
- Mọi chapter/episode đều link vào volume/season ẩn này → `volumeId` / `seasonId` **không bao giờ null** với oneshot

### Chuyển từ oneshot → series

1. Set `Work.isOneshot = false`
2. Virtual volume/season vẫn còn trong DB (`isVirtual=true`)
3. User có thể rename volume đó hoặc thêm volume mới
4. **Không cần migrate data** — chapters đã có `volumeId` rồi

### Chuyển từ series → oneshot

**Điều kiện bắt buộc**: work hiện tại chỉ có **đúng 1** Volume/Season (soft-delete thì không tính).

Service layer kiểm tra trước khi cho phép:

```
MANGA/NOVEL : COUNT(manga_volumes / novel_volumes)  WHERE work_id = ? AND deleted_at IS NULL = 1
ANIME       : COUNT(anime_seasons)                  WHERE work_id = ? AND deleted_at IS NULL = 1
```

Nếu điều kiện thoả:

1. Set `Work.isOneshot = true`
2. Set volume/season hiện có `isVirtual = true`
3. Chapters/Episodes đã có `volumeId`/`seasonId` → không cần thay đổi data

Nếu không thoả → trả về lỗi, **không cho phép** chuyển.

---

## Metadata layer

### WorkLocalization

Bản dịch metadata (tên, synopsis) của Work theo từng ngôn ngữ.

- **Unique**: `(workId, language)` — 1 ngôn ngữ, 1 record
- **Workflow**: `PENDING → APPROVED / REJECTED`
- `submittedBy` = creatorProfileId của người submit
- Khi Work được tạo với `originalLanguage = "jp"`: tự tạo 1 `WorkLocalization` với `language="jp"` và `status=APPROVED`

### WorkGenre

- **Unique**: `(workId, genreId)`

### WorkStaff

Người tham gia sản xuất — chỉ dành cho người thật (tác giả, hoạ sĩ, đạo diễn...).

- **Unique**: `(workId, peopleId, role)`
- `StaffRole`: `AUTHOR / ARTIST / DIRECTOR / CHARACTER_DESIGN`
- Voice actor (seiyuu) **không** nằm ở đây → xem `WorkCharacterDub`

### WorkOrganization

- **Unique**: `(workId, orgId, role)` — cùng org có thể giữ nhiều vai trò
- `OrgRole`: `STUDIO / PUBLISHER / LICENSOR / PRODUCER / SERIALIZER`

### WorkCharacter + WorkCharacterDub

```
WorkCharacter (workId, characterId, role: MAIN/SUPPORTING/BACKGROUND)
  └── WorkCharacterDub (workCharacterId, voiceActorId, language)
      Unique: (workCharacterId, voiceActorId)
```

- 1 nhân vật có thể được lồng tiếng bởi **nhiều seiyuu** (JP/EN/VI dub, hoặc giọng trẻ/lớn)
- Cùng seiyuu không được list 2 lần cho cùng nhân vật

---

## Content layer — MANGA

```
MangaVolume (workId, volumeNumber, isVirtual)
  └── MangaChapter (workId*, volumeId?, chapterNumber:float, isExtra)
      ├── MangaChapterPage (chapterId, pageNumber, imageUrl)
      └── MangaChapterLocalization (chapterId, language, submittedBy)
          └── MangaChapterLocalizationPage (localizationId, pageNumber, imageUrl)
```

*`workId` giữ trực tiếp trên Chapter để query không cần JOIN qua Volume*
*`volumeId` nullable — chapters chưa được xếp vào volume*

### MangaChapter — quy tắc chapterNumber

- Kiểu `float` (PostgreSQL `real`) — hỗ trợ chapter 10.5, 12.5
- `isExtra=true` cho gaiden, side story, omake
- **Không thực hiện arithmetic** khi insert `chapterNumber` — luôn dùng giá trị thẳng từ DTO
- **Unique**: `(workId, chapterNumber)`

### MangaChapterLocalization — workflow cộng đồng

- **Unique**: `(chapterId, language, submittedBy)` — nhiều translator có thể cạnh tranh dịch cùng chapter+language
- Workflow: `DRAFT → PENDING → APPROVED / REJECTED`
- Chỉ 1 localization được `APPROVED` tại 1 thời điểm (enforce ở service layer)
- Khi soft-delete `MangaChapterLocalization`: service layer phải tự soft-delete các `MangaChapterLocalizationPage` con

---

## Content layer — NOVEL

```
NovelVolume (workId, volumeNumber, isVirtual)
  └── NovelChapter (workId*, volumeId?, chapterNumber:float, wordCount, isExtra)
      └── NovelChapterLocalization (chapterId, language, content:jsonb, submittedBy)
```

### NovelChapterLocalization

- `content` = rich text từ plate editor, kiểu `Map<String, Object>` jsonb
- `wordCount` = số từ bản dịch (optional, để tracking tiến độ)
- Workflow và unique constraint giống `MangaChapterLocalization`

---

## Content layer — ANIME

```
AnimeSeason (workId, seasonNumber, year, cour, episodeCount, isVirtual)
  └── AnimeEpisode (workId*, seasonId, episodeNumber:float, durationSeconds, isRecap)
      └── AnimeEpisodeStream (episodeId, provider, url, language)
```

*`workId` denormalized trên Episode — tránh JOIN qua Season khi query toàn bộ tập của 1 work*

### AnimeSeason

- `cour`: `SPRING / SUMMER / FALL / WINTER`
- `episodeCount` = số tập **dự kiến** (có thể khác số tập thực tế trong DB)

### AnimeEpisode

- `episodeNumber` kiểu `float` — hỗ trợ tập recap 5.5
- `durationSeconds` — đơn vị **giây** (UI tự format sang "24 phút")
- `isRecap=true` cho tập tóm tắt
- **Unique**: `(seasonId, episodeNumber)`

### AnimeEpisodeStream

- 1 tập có thể có nhiều stream: JP raw, EN sub, VI sub từ các provider khác nhau
- **Unique**: `(episodeId, provider, language)`
- `StreamProvider`: `YOUTUBE / CRUNCHYROLL / BILIBILI / OTHER`

---

## Soft delete — lưu ý cascade

Tất cả entity đều dùng `@SQLRestriction("deleted_at IS NULL")` — soft delete **không tự cascade** xuống entity con.
Service layer phải xử lý:

| Khi soft-delete            | Phải soft-delete thêm                                      |
|----------------------------|------------------------------------------------------------|
| `MangaChapterLocalization` | → `MangaChapterLocalizationPage`                           |
| `MangaChapter`             | → `MangaChapterPage`, `MangaChapterLocalization` (+ pages) |
| `AnimeSeason`              | → `AnimeEpisode` (+ streams)                               |
| `Work`                     | → tất cả entity con                                        |

---

## Unique constraint + soft delete

Unique constraint trong PostgreSQL **không** lọc `deleted_at IS NULL`. Nếu xóa mềm rồi tạo lại cùng `chapterNumber` /
`seasonNumber`, DB sẽ báo lỗi `DataIntegrityViolationException`.

Xử lý ở service layer: kiểm tra tồn tại trước khi insert (bao gồm cả bản đã soft-delete).
