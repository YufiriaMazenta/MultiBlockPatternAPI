# MultiBlockPatternAPI

基于 Paper API 的多方块结构模式匹配插件，支持 YAML 配置和 Java API 两种方式定义模式。

## 功能特性

- 字符串模式定义（类似配方）
- 水平/竖直方向支持
- 4方向旋转 + 镜像
- BlockMatcher 统一匹配（Material / Predicate）
- 多种触发方式：方块变更、活塞推动、玩家交互
- 无触发方块（放置任意方块时自动检测）
- YAML 配置 + Java API

## 命令

| 命令 | 说明 |
|------|------|
| `/mbp list` | 列出所有已注册模式 |
| `/mbp test` | 测试当前位置匹配 |
| `/mbp reload` | 重新加载配置 |

## YAML 配置示例

### 单层模式（水平方向）

```yaml
simple_altar:
  display_name: "简易祭坛"
  rotation: true

  layer:
    - "L L"
    - " B "
    - "L L"

  map:
    L: OAK_LOG
    B: BEACON

  actions:
    - type: destroy
      drop_items: true
```

### 带触发方块

```yaml
iron_golem:
  display_name: "铁傀儡"
  rotation: false

  layer:
    - " P "
    - "IBI"
    - " I "

  map:
    I: IRON_BLOCK
    B: CARVED_PUMPKIN
    P: JACK_O_LANTERN

  trigger: P

  actions:
    - type: destroy
      drop_items: false
    - type: message
      text: "&a铁傀儡已生成!"
```

### 竖直模式（下界传送门）

```yaml
nether_portal:
  display_name: "下界传送门"
  rotation: true
  direction: vertical

  layer:
    - "OOOOO"
    - "O___O"
    - "O___O"
    - "O___O"
    - "OOOOO"

  map:
    O: OBSIDIAN
    _: AIR

  actions:
    - type: destroy
      drop_items: true
    - type: message
      text: "&a下界传送门已匹配!"
```

### 多层模式（3D结构）

```yaml
treasure_room:
  display_name: "宝藏房间"
  rotation: false

  layers:
    -  # Layer 0
      - "SSSSS"
      - "S___S"
      - "S___S"
      - "S___S"
      - "SSSSS"
    -  # Layer 1
      - "S___S"
      - "_____"
      - "_____"
      - "_____"
      - "S___S"
    -  # Layer 2
      - "SSSSS"
      - "S___S"
      - "S_C_S"
      - "S___S"
      - "SSSSS"

  map:
    S: STONE_BRICKS
    _: AIR
    C: CHEST

  actions:
    - type: destroy
      drop_items: true
```

## YAML 配置字段说明

| 字段 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `display_name` | String | =id | 显示名称 |
| `rotation` | boolean | false | 是否启用旋转匹配 |
| `direction` | String | horizontal | `horizontal`（水平）或 `vertical`（竖直） |
| `layer` | List\<String\> | - | 单层模式定义 |
| `layers` | List\<List\<String\>\> | - | 多层模式定义 |
| `map` | Map | - | 字符到 Material 的映射 |
| `trigger` | String | - | 触发方块对应的字符 |
| `trigger_type` | String | block_change | `block_change`（方块变更）或 `interaction`（玩家交互） |
| `actions` | List | - | 匹配后执行的动作 |

### 特殊字符

| 字符 | 含义 |
|------|------|
| 空格 ` ` | 通配符，匹配任意方块（不加入 matchedBlocks） |
| `_` | 匹配空气方块（加入 matchedBlocks） |

### Actions 类型

| type | 参数 | 说明 |
|------|------|------|
| `destroy` | `drop_items: true/false` | 破坏匹配的方块 |
| `message` | `text: "消息"` | 发送消息给所有玩家 |

## Java API 示例

### 基本用法

```java
import org.bukkit.Material;
import pers.yufiria.multiblockpatternapi.api.*;
import pers.yufiria.multiblockpatternapi.registry.PatternRegistry;

// 创建模式
MultiBlockPattern pattern = PatternBuilder.create("my_pattern")
    .displayName("我的模式")
    .layer(
        "L L",
        " B ",
        "L L"
    )
    .block('L', BlockMatcher.ofMaterial(Material.OAK_LOG))
    .block('B', BlockMatcher.ofMaterial(Material.BEACON))
    .action(result -> {
        // 自定义处理逻辑
        result.getOrigin().getWorld().getPlayers().forEach(p ->
            p.sendMessage("模式已匹配!")
        );
    })
    .rotationEnabled(true)
    .build();

// 注册模式
PatternRegistry.INSTANCE.register(pattern);
```

### 竖直模式

```java
MultiBlockPattern portal = PatternBuilder.create("my_portal")
    .displayName("传送门")
    .direction(MultiBlockPattern.Direction.VERTICAL)
    .layer(
        "OOOOO",
        "O___O",
        "O___O",
        "O___O",
        "OOOOO"
    )
    .block('O', BlockMatcher.ofMaterial(Material.OBSIDIAN))
    .action(result -> {})
    .rotationEnabled(true)
    .build();

PatternRegistry.INSTANCE.register(portal);
```

### 带触发方块（方块变更触发）

```java
MultiBlockPattern golem = PatternBuilder.create("my_golem")
    .displayName("铁傀儡")
    .triggerType(TriggerType.BLOCK_CHANGE)
    .triggerBlock('P', BlockMatcher.ofMaterial(Material.JACK_O_LANTERN))
    .layer(
        " P ",
        "IBI",
        " I "
    )
    .block('I', BlockMatcher.ofMaterial(Material.IRON_BLOCK))
    .block('B', BlockMatcher.ofMaterial(Material.CARVED_PUMPKIN))
    .action(result -> {
        // 触发方块放置时执行
    })
    .build();

PatternRegistry.INSTANCE.register(golem);
```

### Predicate 自定义匹配

```java
MultiBlockPattern wood = PatternBuilder.create("any_wood")
    .displayName("任意木材")
    .layer(
        "W W",
        " W "
    )
    .block('W', BlockMatcher.ofPredicate(block ->
        block.getType().name().endsWith("_LOG")
    ))
    .action(result -> {})
    .build();

PatternRegistry.INSTANCE.register(wood);
```

### 交互触发模式

```java
// 手持木棍右键石头时触发
MultiBlockPattern magicAltar = PatternBuilder.create("magic_altar")
    .displayName("魔法祭坛")
    .triggerType(TriggerType.INTERACTION)
    .triggerBlock('B', BlockMatcher.ofMaterial(Material.BEACON))
    .internalCondition((block, player) ->
        block.getType() == Material.STONE &&
        player.getInventory().getItemInMainHand().getType() == Material.STICK
    )
    .layer(
        " S ",
        "SBS",
        " S "
    )
    .block('S', BlockMatcher.ofMaterial(Material.STONE))
    .block('B', BlockMatcher.ofMaterial(Material.BEACON))
    .action(result -> {})
    .build();

PatternRegistry.INSTANCE.register(magicAltar);
```

### 事件监听

```java
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import pers.yufiria.multiblockpatternapi.api.event.MultiblockMatchEvent;

public class MyListener implements Listener {

    @EventHandler
    public void onMatch(MultiblockMatchEvent event) {
        MatchResult result = event.getResult();
        Invoker causer = result.getCauser();
        Block triggerBlock = result.getTriggerBlock();

        // causer 可能为null（非玩家触发）
        if (causer != null) {
            causer.sendMsg("检测到结构: " + result.getPattern().getDisplayName());
        }
    }
}
```

## PatternBuilder API

| 方法 | 说明 |
|------|------|
| `create(id)` | 创建构建器 |
| `displayName(name)` | 设置显示名称 |
| `layer(rows...)` | 添加一层（水平方向） |
| `direction(dir)` | 设置方向（VERTICAL/HORIZONTAL） |
| `block(char, matcher)` | 映射字符到 BlockMatcher |
| `triggerBlock(char, matcher)` | 设置触发点（锚点） |
| `triggerType(type)` | 设置触发类型（BLOCK_CHANGE/INTERACTION） |
| `internalCondition(biPredicate)` | 设置交互触发条件 |
| `action(callback)` | 注册匹配回调 |
| `rotationEnabled(bool)` | 启用旋转 |
| `build()` | 构建模式 |

## MatchResult API

| 方法 | 说明 |
|------|------|
| `isMatch()` | 是否匹配成功 |
| `getPattern()` | 获取匹配的模式 |
| `getOrigin()` | 获取原点位置 |
| `getRotation()` | 获取匹配时的旋转方向 |
| `getMatchedBlocks()` | 获取匹配的方块列表（不含通配符） |
| `getCauser()` | 获取触发者（可为null，非玩家触发时） |
| `getTriggerBlock()` | 获取触发方块（放置/交互的方块） |
| `execute()` | 执行所有注册的 action |

## 构建

```bash
gradle build
```
