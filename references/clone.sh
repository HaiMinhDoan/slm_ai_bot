#!/usr/bin/env bash
# Clone (hoặc cập nhật) các repo / MCP dùng làm tài liệu tham khảo cho các adapter Java.
# Quy tắc (kế hoạch mục 46.1): chỉ đọc để port sang Java. KHÔNG cài đặt, build, chạy
# hay import code trong các thư mục này vào ứng dụng.
#
# Dùng:  bash references/clone.sh            # clone những repo còn thiếu
#        bash references/clone.sh --update   # cập nhật repo đã có lên commit mới nhất
set -euo pipefail

cd "$(dirname "$0")"
UPDATE="${1:-}"
export GIT_LFS_SKIP_SMUDGE=1 GIT_TERMINAL_PROMPT=0

# thư mục đích | URL git
REPOS=(
  "facebook/meta-mcp-server|https://github.com/oliverames/meta-mcp-server.git"
  "facebook/facebook-mcp-server|https://github.com/HagaiHen/facebook-mcp-server.git"
  "zalo/zalo-java-sdk|https://github.com/zaloplatform/zalo-java-sdk.git"
  "zalo/demo-e2ee-zns-csharp|https://github.com/zaloplatform/demo-e2ee-zns-CSharp.git"
  "tiktok/tiktok-business-api-docs|https://github.com/justomat/tiktok-business-api-docs.git"
  "tiktok/tiktok-mcp-postonce|https://github.com/postoncehq/tiktok-mcp.git"
  "tiktok/tiktok-mcp-hasdata|https://github.com/HasData/tiktok-mcp.git"
  "rui-ro-cao/facebook-go|https://github.com/teslashibe/facebook-go.git"
  "rui-ro-cao/zca-js|https://github.com/RFS-ADRENO/zca-js.git"
)

# thư mục đích | URL tarball npm (gói không có repo GitHub công khai)
NPM_PACKAGES=(
  "zalo/redai-zalo-sdk|https://registry.npmjs.org/@warriorteam/redai-zalo-sdk/-/redai-zalo-sdk-1.39.4.tgz"
)

for entry in "${REPOS[@]}"; do
  dir="${entry%%|*}"; url="${entry#*|}"
  if [[ -d "$dir/.git" ]]; then
    if [[ "$UPDATE" == "--update" ]]; then
      echo "update  $dir"
      git -C "$dir" fetch --depth 1 --quiet origin
      git -C "$dir" reset --hard --quiet FETCH_HEAD
    else
      echo "skip    $dir (đã có)"
    fi
  else
    echo "clone   $dir"
    mkdir -p "$(dirname "$dir")"
    git clone --depth 1 --quiet --no-recurse-submodules "$url" "$dir"
  fi
done

for entry in "${NPM_PACKAGES[@]}"; do
  dir="${entry%%|*}"; url="${entry#*|}"
  if [[ -d "$dir" && "$UPDATE" != "--update" ]]; then
    echo "skip    $dir (đã có)"; continue
  fi
  echo "download $dir"
  rm -rf "$dir"; mkdir -p "$dir"
  curl -fsSL "$url" | tar -xz -C "$dir" --strip-components=1   # chỉ giải nén, không chạy npm install
done

echo
echo "Commit / phiên bản đang có:"
for entry in "${REPOS[@]}"; do
  dir="${entry%%|*}"
  printf '  %-34s %s\n' "$dir" "$(git -C "$dir" log -1 --format='%h  %cs')"
done
for entry in "${NPM_PACKAGES[@]}"; do
  dir="${entry%%|*}"
  ver="${entry##*-}"; printf '  %-34s npm %s\n' "$dir" "${ver%.tgz}"
done
