# README: portfolio.toml 讀取與寫入功能

## 簡述

此功能使用 ==portfolio.toml== 檔案的讀取與寫入操作，用於儲存和管理使用者的資產資料。寫入功能在 ==InputsFragment== 中，讀取功能在 ==OutputsFragment== 中。

## 相關檔案

1. ==InputsFragment.kt==
    * 路徑：==app/src/main/java/tw/edu/niu/investment_android/ui/inputs/InputsFragment.kt==
    * 功能：處理使用者輸入的資產資料，將其寫入 ==portfolio.toml==。
2. ==OutputsFragment.kt==
    * 路徑：==app/src/main/java/tw/edu/niu/investment_android/ui/outputs/OutputsFragment.kt==
    * 功能：從 ==portfolio.toml== 讀取資產資料並顯示。
3. ==portfolio.toml==
    * 路徑：==/data/data/tw.edu.niu.investment_android/files/portfolio.toml==
    * 功能：儲存資產資料，格式為 TOML。

## 功能描述

### 寫入功能（==InputsFragment.kt==）

* 檔案路徑：==portfolio.toml== 儲存在應用內部儲存目錄 ==/data/data/tw.edu.niu.investment_android/files/portfolio.toml==。
* 寫入邏輯：
    * 使用者在 ==InputsFragment== 中選擇分類（例如 "US Stock"）、輸入標的符號（例如 "amd"）和數量（例如 "10"），然後點擊「確認」。
    * 分類會轉換為 TOML 表名（例如 ==us-stock==），並以 ==[us-stock]== 的格式寫入。
    * 標的和數量以 ==symbol = amount== 格式寫入（例如 ==amd = 10==）。
    * 若標的已存在，則更新其數量；若分類不存在，則建立新表。

* 格式規範：
    * 數字（例如 ==10== 或 ==0.5==）不加引號，符合 TOML 規範。

### 讀取功能（==OutputsFragment.kt==）
* 檔案路徑：從內部儲存的 ==portfolio.toml== 讀取資料。
* 讀取邏輯：
    * 解析 ==portfolio.toml==，提取每個分類（表名）及其下的標的和數量。
    * 將資料轉換為 ==Asset== 物件（包含分類、符號、數量），並用於顯示。
    * 處理 TOML 格式的引號（例如移除 =="2330"== 的引號，顯示為 ==2330==）。

## 檔案格式示例

假設使用者新增以下資產：
* US Stock: ==amd = 10==
* US ETF: ==QQQ = 20==
* TW Stock: ==2330 = 10==
* TW ETF: ==0050 = 20==
* Crypto: ==eth = 0.5==, ==sol = 0.5==

生成的 ==portfolio.toml== 內容如下：
```toml
[us-stock]
amd = 10

[us-etf]
QQQ = 20

[tw-stock]
2330 = 10

[tw-etf]
0050 = 20

[crypto]
eth = 0.5
sol = 0.5
```

