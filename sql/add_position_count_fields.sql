-- 岗位人数控制功能 - 数据库修改

-- 添加招聘总人数和剩余人数字段
ALTER TABLE position
ADD COLUMN total_positions INT DEFAULT 1 COMMENT '招聘总人数',
ADD COLUMN remaining_positions INT DEFAULT 1 COMMENT '剩余招聘人数';

-- 更新现有数据，设置默认值
UPDATE position 
SET total_positions = 1, remaining_positions = 1 
WHERE total_positions IS NULL;
