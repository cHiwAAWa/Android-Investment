# README: portfolio.toml 讀取與寫入功能

## 簡述

此功能使用 <mark>portfolio.toml</mark> 檔案的讀取與寫入操作，用於儲存和管理使用者的資產資料。寫入功能在 <mark>InputsFragment</mark> 中，讀取功能在 <mark>OutputsFragment</mark> 中。

## 相關檔案

1. <mark>InputsFragment.kt</mark>
    * 路徑：<mark>app/src/main/java/tw/edu/niu/investment_android/ui/inputs/InputsFragment.kt</mark>
    * 功能：處理使用者輸入的資產資料，將其寫入 <mark>portfolio.toml</mark>。
2. <mark>OutputsFragment.kt</mark>
    * 路徑：<mark>app/src/main/java/tw/edu/niu/investment_android/ui/outputs/OutputsFragment.kt</mark>
    * 功能：從 <mark>portfolio.toml</mark> 讀取資產資料並顯示。
3. <mark>portfolio.toml</mark>
    * 路徑：<mark>/data/data/tw.edu.niu.investment_android/files/portfolio.toml</mark>
    * 功能：儲存資產資料，格式為 TOML。

## 功能描述

### 寫入功能（<mark>InputsFragment.kt</mark>）

* 檔案路徑：<mark>portfolio.toml</mark> 儲存在應用內部儲存目錄 <mark>/data/data/tw.edu.niu.investment_android/files/portfolio.toml</mark>。
* 寫入邏輯：
    * 使用者在 <mark>InputsFragment</mark> 中選擇分類（例如 "US Stock"）、輸入標的符號（例如 "amd"）和數量（例如 "10"），然後點擊「確認」。
    * 分類會轉換為 TOML 表名（例如 <mark>us-stock</mark>），並以 <mark>[us-stock]</mark> 的格式寫入。
    * 標的和數量以 <mark>symbol = amount</mark> 格式寫入（例如 <mark>amd = 10</mark>）。
    * 若標的已存在，則更新其數量；若分類不存在，則建立新表。

* 格式規範：
    * 數字（例如 <mark>10</mark> 或 <mark>0.5</mark>）不加引號，符合 TOML 規範。

### 讀取功能（<mark>OutputsFragment.kt</mark>）
* 檔案路徑：從內部儲存的 <mark>portfolio.toml</mark> 讀取資料。
* 讀取邏輯：
    * 解析 <mark>portfolio.toml</mark>，提取每個分類（表名）及其下的標的和數量。
    * 將資料轉換為 <mark>Asset</mark> 物件（包含分類、符號、數量），並用於顯示。
    * 處理 TOML 格式的引號（例如移除 <mark>"2330"</mark> 的引號，顯示為 <mark>2330</mark>）。

## 檔案格式示例

假設使用者新增以下資產：
* US Stock: <mark>amd = 10</mark>
* US ETF: <mark>QQQ = 20</mark>
* TW Stock: <mark>2330 = 10</mark>
* TW ETF: <mark>0050 = 20</mark>
* Crypto: <mark>eth = 0.5</mark>, <mark>sol = 0.5</mark>

生成的 <mark>portfolio.toml</mark> 內容如下：
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

