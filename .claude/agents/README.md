# Agents

6 agent tanımları ve görev tanımları içeren merkezi doküman.

## Agent Listesi

| # | Agent | Rol | Çıktı |
|---|-------|-----|-------|
| 1 | [Design & Analysis](./design-analysis-agent.md) | Web araştırması, AR örnekleri, tasarım | Tasarım dokümanı |
| 2 | [Android Expert](./android-expert-agent.md) | ARCore araştırması, Android raporu | Android raporu |
| 3 | [iOS Expert](./ios-expert-agent.md) | ARKit araştırması, iOS raporu | iOS raporu |
| 4 | [Main Developer](./main-developer-agent.md) | Kod geliştirme (domain, data, presentation) | Kod dosyaları |
| 5 | [Test Developer](./test-developer-agent.md) | Birim testleri yazma | Test dosyaları |
| 6 | [Code Reviewer](./code-reviewer-agent.md) | Kod standartları kontrolü | Review raporu |

## Agent İletişim Flow

```
Design & Analysis Agent (araştırma + tasarım)
        ↓
    ↓                  ↓
Android Expert    iOS Expert
(rapor)          (rapor)
    ↓                  ↓
    ↓←←←←Main Developer→→→→↓
    ↓                  ↓
    ↓←Test Developer (testler)
    ↓
Code Reviewer (kalite kontrol)
    ↓
Main Developer (düzeltmeler)
```

## Proje Bilgileri

**Proje:** ARCore (Android) ve ARKit (iOS) kullanarak 3D objeleri import edip ekleyip çıkarabilecek sample uygulama.

**Mimari:** DDD + Clean Architecture + MVVM

**Platform:** Kotlin Multiplatform (Android + iOS)

**Model Format:** GLB (tercih edilen), USDZ (iOS)

**Storage:** Local persistence (app açılıp kapandığında objeler kayıtlı kalır)

## Detaylı Agent Dokümanları

1. [Design & Analysis Agent](./design-analysis-agent.md) - Tasarım ve araştırma
2. [Android Expert Agent](./android-expert-agent.md) - Android ARCore implementasyonu
3. [iOS Expert Agent](./ios-expert-agent.md) - iOS ARKit implementasyonu
4. [Main Developer Agent](./main-developer-agent.md) - Ana kod geliştirme
5. [Test Developer Agent](./test-developer-agent.md) - Birim testleri
6. [Code Reviewer Agent](./code-reviewer-agent.md) - Kod kalite kontrolü