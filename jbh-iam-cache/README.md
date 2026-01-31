# jbh-iam-cache

Local caching module for JBH IAM service using Caffeine with hexagonal architecture.

## Objective

Provide a high-performance, in-memory caching layer for user data to reduce database load and improve response times. The module is designed with technology abstraction in mind, allowing future migration to distributed caches (Redis, Hazelcast) without changing the consuming code.

## Library: Caffeine

**Why Caffeine?**

| Criteria | Caffeine | Ehcache 3 | Guava Cache |
|----------|----------|-----------|-------------|
| Performance | Excellent (W-TinyLFU) | Good | Moderate |
| Memory efficiency | High | Medium | Medium |
| Spring integration | Native | Good | Manual |
| Stats/monitoring | Built-in | Built-in | Basic |
| Maintenance | Active | Active | Maintenance mode |

Caffeine uses the **W-TinyLFU** eviction algorithm which provides near-optimal hit rates. It's the recommended cache for Spring Boot applications.

## Architecture

```
jbh-iam-cache/
├── api/                          # Public interfaces (what consumers use)
│   ├── CacheAdapter.kt           # Generic cache operations
│   ├── UserCacheOperations.kt    # User-specific operations
│   ├── CacheStatsOperations.kt   # Statistics operations
│   └── dto/
│       ├── CachedUserDTO.kt      # User cache DTO (no password)
│       └── CacheStatsDTO.kt      # Statistics DTO
├── domain/
│   ├── port/outbound/            # Secondary ports (abstractions)
│   │   ├── CacheStoragePort.kt   # Storage abstraction
│   │   └── CacheStatsPort.kt     # Stats abstraction
│   └── model/
│       └── CacheStats.kt         # Domain model
├── application/service/          # Use cases
│   ├── UserCacheService.kt       # Implements UserCacheOperations
│   └── CacheStatsService.kt      # Implements CacheStatsOperations
└── infrastructure/
    ├── adapter/outbound/caffeine/
    │   ├── CaffeineCacheStorageAdapter.kt
    │   └── CaffeineCacheStatsAdapter.kt
    └── config/
        ├── CacheConfig.kt        # Spring configuration
        └── CacheProperties.kt    # Configuration properties
```

## Public API

### UserCacheOperations

```kotlin
interface UserCacheOperations : CacheAdapter<UUID, CachedUserDTO> {
    fun getByEmail(email: String): CachedUserDTO?
    fun getByNickName(nickName: String): CachedUserDTO?
    fun getByNickNameOrEmail(nickName: String, email: String): CachedUserDTO?
    fun getAllByIds(ids: List<UUID>): List<CachedUserDTO>
    fun evictUser(userId: UUID, email: String, nickName: String)
}
```

### CacheStatsOperations

```kotlin
interface CacheStatsOperations {
    fun getUserCacheStats(): CacheStatsDTO
    fun getAllCacheStats(): Map<String, CacheStatsDTO>
}
```

### CacheStatsDTO

```kotlin
data class CacheStatsDTO(
    val cacheName: String,
    val hitCount: Long,
    val missCount: Long,
    val hitRate: Double,          // Percentage (0-100)
    val evictionCount: Long,
    val currentSize: Long,
    val maxSize: Long,
    val usagePercentage: Double   // Percentage (0-100)
)
```

## REST Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/admin/cache/stats` | All cache statistics | ADMIN |
| GET | `/admin/cache/stats/users` | User cache statistics | ADMIN |

## Configuration

```yaml
# application.yml
jbh:
  cache:
    user:
      max-size: 5000                      # Maximum cached entries
      expire-after-write-minutes: 60      # TTL after write
      expire-after-access-minutes: 30     # TTL after last access
      enabled: true                       # Enable/disable cache
```

Environment variables:
- `CACHE_USER_MAX_SIZE` - Default: 5000
- `CACHE_USER_EXPIRE_WRITE_MINUTES` - Default: 60
- `CACHE_USER_EXPIRE_ACCESS_MINUTES` - Default: 30
- `CACHE_USER_ENABLED` - Default: true

## Usage Example

```kotlin
@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val userCache: UserCacheOperations
) : UserService {

    override fun findById(id: UUID): InternalUserDTO {
        return userCache.getOrLoad(id) {
            userRepository.findById(id).orElse(null)
                ?.toInternalUserDTO()?.toCachedDTO()
        }?.toInternalDTO()
            ?: throw IllegalArgumentException("User not found")
    }

    override fun save(user: UserEntity): InternalUserDTO {
        val saved = userRepository.save(user).toInternalUserDTO()
        userCache.put(saved.id, saved.toCachedDTO())
        return saved
    }

    fun delete(dto: InternalUserDTO) {
        userRepository.deleteById(dto.id)
        userCache.evictUser(dto.id, dto.email, dto.nickName)
    }
}
```

## Secondary Indexes

The module maintains secondary indexes for O(1) lookups:
- Primary: `UUID -> CachedUserDTO`
- Email index: `email (lowercase) -> UUID`
- NickName index: `nickName (lowercase) -> UUID`

All indexes are automatically synchronized on put/evict operations.

## Dependencies

```kotlin
// build.gradle.kts
dependencies {
    implementation(project(":jbh-iam-core"))
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")
    implementation("org.springframework.boot:spring-boot-starter-cache")
}
```
