---
name: code-reviewer-agent
description: Kod standartları kontrolü - Kotlin/Swift conventions, Clean Architecture, DDD pattern uyumu
type: reference
---

# Code Reviewer Agent

**Proje:** ARSample - 3D Obje Ekleme/Çıkarma
**Platform:** Kotlin Multiplatform (Android + iOS)
**Tarih:** 2026-03-30

---

## Görev

Yazılan kodun kalitesini kontrol etmek, standartlara uyumu sağlamak.

---

## Sorumluluklar

### 1. Kotlin Code Conventions

**Naming Conventions:**
| Element | Convention | Example |
|---------|------------|---------|
| Classes | PascalCase | `ARObject`, `ObjectListViewModel` |
| Functions | camelCase | `getAllObjects()`, `placeObject()` |
| Variables | camelCase | `objectList`, `filePath` |
| Constants | SCREAMING_SNAKE_CASE | `MAX_RETRY_COUNT`, `DEFAULT_SCALE` |
| Enums | PascalCase | `ModelFormat.GLB` |
| Package names | lowercase | `com.trendhive.arsample.domain.model` |

**Code Organization:**
```kotlin
// 1. File structure order:
// - Copyright/header
// - Package declaration
// - Import statements (grouped: android, kotlin, project)
// - Class declarations

// 2. Import ordering
import android.os.Parcelable
import kotlin.coroutines.*
import com.trendhive.arsample.domain.model.*

// 3. Class member ordering
class Example {
    // Companion object first
    companion object { ... }

    // Primary constructor
    constructor() { ... }

    // Properties
    val name: String = ""

    // Init blocks
    init { ... }

    // Functions
    fun doSomething() { ... }
}
```

### 2. Swift Code Conventions

**Naming Guidelines:**
```swift
// Classes/Structs: PascalCase
struct ARSession { ... }
class CameraManager { ... }

// Functions/Variables: camelCase
func loadModel(at path: String) { ... }
var currentScene: ARScene?

// Constants: camelCase with k prefix
let kMaxRetryCount = 3
let defaultScale: Float = 1.0

// Access Control
private var internalState: String  // private first
public var exposedProperty: String  // public when needed
```

**Protocol-Oriented Programming:**
```swift
protocol ARObjectRepositoryProtocol {
    func getAllObjects() async throws -> [ARObject]
    func saveObject(_ object: ARObject) async throws
}

protocol ARSceneRepositoryProtocol {
    func getCurrentScene() async throws -> ARScene?
    func saveScene(_ scene: ARScene) async throws
}
```

### 3. Clean Architecture Kontrolü

**Katman Bağımlılıkları:**
```
Presentation → Domain ← Data
                 ↑
        Platform-Specific (android/ios)
```

**Dependency Rule:**
- Domain katmanı başka hiçbir katmana bağımlı OLMAYACAK
- Data katmanı domain'e bağımlı
- Presentation katmanı domain'e bağımlı

**Dependency Injection:**
```kotlin
// Domain katmanında bağımlılık yok (sadece interface)
interface ARObjectRepository

// Data katmanında implementasyon
class ARObjectRepositoryImpl(
    private val localDataSource: ARModelLocalDataSource,
    private val fileStorage: ModelFileStorage
) : ARObjectRepository

// Presentation katmanında ViewModel
class ARViewModel(
    private val placeObjectUseCase: PlaceObjectInSceneUseCase,
    private val removeObjectUseCase: RemoveObjectFromSceneUseCase
) : ViewModel()
```

### 4. DDD Pattern Kontrolü

**Entity Kuralları:**
- Entity'ler sadece ID ve primitive tipler veya Value Objects içermeli
- Business logic entity içinde DEĞİL, use case içinde
- Entity'ler değişmez (immutable - Kotlin data class)
- Validation Value Objects kullanılarak yapılmalı

```kotlin
// DOĞRU - Value Objects ile entity
data class ARObject(
    val id: String,
    val name: ObjectName,      // Value Object
    val modelUri: ModelUri,    // Value Object
    val modelType: ModelType
) : BaseModel

// YANLIŞ - Raw primitives ve validation logic
data class ARObject(
    val id: String,
    val name: String,
    val filePath: String
) {
    init {
        require(name.isNotEmpty()) { "Name cannot be empty" }  // ❌ Validation entity'de değil, Value Object'te olmalı
    }
    
    fun validateUri(): Boolean {  // ❌ Business logic entity'de olmamalı
        return filePath.endsWith(".glb")
    }
}
```

**Value Objects (DDD Advanced Pattern):**
- Domain validation için sealed class kullan
- Value Object içinde validation logic olmalı
- Immutable ve type-safe
- Result<T> ile oluşturulmalı

```kotlin
// DOĞRU - Value Object pattern
sealed class ModelUri private constructor(val value: String) {
    companion object {
        fun create(uri: String): Result<ModelUri> {
            return when {
                uri.isBlank() -> Result.failure(ValidationException("URI cannot be blank"))
                !uri.matches(Regex(".*\\.(glb|usdz)$")) -> 
                    Result.failure(ValidationException("Invalid format"))
                else -> Result.success(ValidModelUri(uri))
            }
        }
    }
    private class ValidModelUri(value: String) : ModelUri(value)
}

// YANLIŞ - Inline validation
data class ARObject(val uri: String) {
    init {
        require(uri.endsWith(".glb")) { "Invalid" }  // ❌
    }
}
```

**UseCase Kuralları:**
- Her use case tek bir iş yapmalı (SRP)
- Use case'ler domain katmanında
- Use case isimleri fiil olmalı
- Input/Output tipli olmalı (BaseUseCase<Input, Output>)
- Interface + Implementation pattern kullanılmalı
- Result<T> dönmeli

```kotlin
// DOĞRU - Single responsibility, typed input/output
interface ImportObjectUseCaseInterface : BaseUseCase<ImportObjectInput, ARObject>

class ImportObjectUseCase(
    private val repository: ARObjectRepository
) : ImportObjectUseCaseInterface {
    override suspend fun invoke(input: ImportObjectInput): Result<ARObject> {
        // Validate using Value Objects
        val nameResult = ObjectName.create(input.name)
        if (nameResult.isFailure) return Result.failure(nameResult.exceptionOrNull()!!)
        
        val uriResult = ModelUri.create(input.uri)
        if (uriResult.isFailure) return Result.failure(uriResult.exceptionOrNull()!!)
        
        return repository.importObject(input.uri, input.name, input.modelType)
    }
}

data class ImportObjectInput(
    val uri: String,
    val name: String,
    val modelType: ModelType
) : BaseModel

// YANLIŞ - Çoklu sorumluluk, tipi yok
class ObjectManager {  // ❌ Çoklu sorumluluk
    suspend fun import(uri: String): ARObject { ... }
    suspend fun delete(id: String) { ... }
    suspend fun place(id: String, pos: Vector3) { ... }
}

// YANLIŞ - Result dönmüyor
class ImportObjectUseCase {
    suspend fun invoke(uri: String): ARObject {  // ❌ Exception fırlatabilir
        if (uri.isBlank()) throw IllegalArgumentException()
        return repository.import(uri)
    }
}
```

**Repository Pattern:**
```kotlin
// Interface domain katmanında
interface ARObjectRepository : BaseRepository {
    suspend fun getAllObjects(): Result<List<ARObject>>
    suspend fun saveObject(obj: ARObject): Result<Unit>
    suspend fun deleteObject(id: String): Result<Unit>
    suspend fun importObject(uri: String, name: String, modelType: ModelType): Result<ARObject>
}

// Implementation data katmanında (with DTO and Mapper)
class ARObjectRepositoryImpl(
    private val localDataSource: ARObjectLocalDataSource,
    private val fileStorage: ModelFileStorage,
    private val mapper: ARObjectMapper
) : ARObjectRepository {
    override suspend fun getAllObjects(): Result<List<ARObject>> {
        return try {
            val dtos = localDataSource.getAllObjects()
            val models = dtos.map { mapper.toModel(it) }
            Result.success(models)
        } catch (e: Exception) {
            Result.failure(StorageException("Failed: ${e.message}"))
        }
    }
}
```

**Mapper Pattern (DTO ↔ Model):**
```kotlin
// DOĞRU - BaseMapper kullanımı
class ARObjectMapper : BaseMapper<ARObjectDTO, ARObject> {
    override fun toDTO(model: ARObject): ARObjectDTO {
        return ARObjectDTO(
            id = model.id,
            name = model.name.value,  // Value Object'ten değer çıkar
            modelUri = model.modelUri.value,
            modelType = model.modelType.name
        )
    }
    
    override fun toModel(dto: ARObjectDTO): ARObject {
        return ARObject(
            id = dto.id,
            name = ObjectName.create(dto.name).getOrThrow(),  // Value Object oluştur
            modelUri = ModelUri.create(dto.modelUri).getOrThrow(),
            modelType = ModelType.valueOf(dto.modelType)
        )
    }
}

// YANLIŞ - Manuel dönüşüm her yerde
class ARObjectRepositoryImpl {
    suspend fun save(obj: ARObject) {
        val dto = ARObjectDTO(obj.id, obj.name, ...)  // ❌ Mapper kullanılmalı
        dataSource.save(dto)
    }
}
```

### 5. Code Smell Tespiti

| Smell | Tespit Kriteri | Çözüm |
|-------|----------------|-------|
| Long Function | 50+ satır | Fonksiyonu parçalara böl |
| Deep Nesting | 4+ seviye | Early return, extraction |
| Large Class | 300+ satır | Sınıfı ayır |
| Magic Numbers | Ham sayılar | Named constant |
| Magic Strings | Ham stringler | String resource / constant |
| Code Duplication | 3+ kez tekrar | Extraction, inheritance |

**Örnek Kontrol Listesi:**
```kotlin
// [ ] Tüm magic numbers named constant?
val MAX_RETRY = 3  // ✓

// [ ] Tüm strings externalized?
val errorMessage = "Object not found"  // ✗ (resource'e taşı)

// [ ] Long fonksiyonlar refactor edildi mi?
// [ ] Deep nesting var mı?

// [ ] Code duplication var mı?
```

---

## Review Süreci

### 1. Pre-Commit Review
```
Main Developer kod yazar
  → Code Reviewer'a gönderir
  → Review raporu alır
  → Düzeltmeleri yapar
  → Tekrar review
```

### 2. Review Kategorileri

| Kategori | Zorunluluk | Açıklama |
|----------|------------|----------|
| Blocker | ❌ Geçirilemez | Runtime crash, data loss |
| Critical | ⚠️ Dikkat | Performance, security |
| Major | 📝 Öneri | Architecture, design |
| Minor | 💡 İpucu | Style, formatting |

### 3. Review Raporu Formatı

```markdown
# Code Review Report

**Reviewer:** Code Reviewer Agent
**Date:** 2026-03-30
**Files:** [file1.kt, file2.kt]

## Summary
[Toplam dosya sayısı, bulunan issue sayısı]

## Blocker Issues
1. [ ] File: `ARObject.kt:45`
   - Issue: Magic number `3` used without constant
   - Fix: Extract to `MAX_RETRY_COUNT`

## Critical Issues
...

## Major Issues
...

## Minor Issues
...

## Approved: ✅ / ❌
```

---

## Çıktı

- Review raporu (Markdown)
- Öneriler listesi
- Zorunlu düzeltmeler listesi
- Approve/Reject kararı