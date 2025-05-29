package com.example.bluecat.controller;

import com.example.bluecat.dto.SCL90QuestionDTO;
import com.example.bluecat.dto.SCL90ResultDTO;
import com.example.bluecat.entity.SCL90Result;
import com.example.bluecat.service.SCL90Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/scl90")
public class SCL90Controller {

    @Autowired
    private SCL90Service scl90Service;

    @GetMapping("/questions")
    public ResponseEntity<List<SCL90QuestionDTO>> getAllQuestions() {
        // 返回所有SCL-90问题列表
        List<SCL90QuestionDTO> questions = new ArrayList<>();
        
        // 添加90个标准SCL-90问题
        questions.add(new SCL90QuestionDTO(1, "1. 头痛", "躯体化"));
        questions.add(new SCL90QuestionDTO(2, "2. 神经过敏，心中不踏实", "焦虑"));
        questions.add(new SCL90QuestionDTO(3, "3. 头脑中有不必要的想法或字句盘旋", "强迫症状"));
        questions.add(new SCL90QuestionDTO(4, "4. 头昏或昏倒", "躯体化"));
        questions.add(new SCL90QuestionDTO(5, "5. 对异性的兴趣减退", "抑郁"));
        questions.add(new SCL90QuestionDTO(6, "6. 对旁人责备求全", "人际关系敏感"));
        questions.add(new SCL90QuestionDTO(7, "7. 感到别人能控制你的思想", "精神病性"));
        questions.add(new SCL90QuestionDTO(8, "8. 责怪别人制造麻烦", "偏执"));
        questions.add(new SCL90QuestionDTO(9, "9. 忘记性大", "强迫症状"));
        questions.add(new SCL90QuestionDTO(10, "10. 担心自己的衣饰整齐及仪态的端正", "强迫症状"));
        questions.add(new SCL90QuestionDTO(11, "11. 容易烦恼和激动", "敌对"));
        questions.add(new SCL90QuestionDTO(12, "12. 胸痛", "躯体化"));
        questions.add(new SCL90QuestionDTO(13, "13. 害怕空旷的场所或街道", "恐怖"));
        questions.add(new SCL90QuestionDTO(14, "14. 感到自己的精力下降，活动减慢", "抑郁"));
        questions.add(new SCL90QuestionDTO(15, "15. 想结束自己的生命", "抑郁"));
        questions.add(new SCL90QuestionDTO(16, "16. 听到旁人听不到的声音", "精神病性"));
        questions.add(new SCL90QuestionDTO(17, "17. 发抖", "焦虑"));
        questions.add(new SCL90QuestionDTO(18, "18. 感到大多数人都不可信任", "偏执"));
        questions.add(new SCL90QuestionDTO(19, "19. 胃口不好", "其他"));
        questions.add(new SCL90QuestionDTO(20, "20. 容易哭泣", "抑郁"));
        questions.add(new SCL90QuestionDTO(21, "21. 同异性相处时感到害羞不自在", "人际关系敏感"));
        questions.add(new SCL90QuestionDTO(22, "22. 感到受骗，中了圈套或有人想抓您", "抑郁"));
        questions.add(new SCL90QuestionDTO(23, "23. 无缘无故地突然感到害怕", "焦虑"));
        questions.add(new SCL90QuestionDTO(24, "24. 自己不能控制地大发脾气", "敌对"));
        questions.add(new SCL90QuestionDTO(25, "25. 怕单独出门", "恐怖"));
        questions.add(new SCL90QuestionDTO(26, "26. 经常责怪自己", "抑郁"));
        questions.add(new SCL90QuestionDTO(27, "27. 腰痛", "躯体化"));
        questions.add(new SCL90QuestionDTO(28, "28. 感到难以完成任务", "强迫症状"));
        questions.add(new SCL90QuestionDTO(29, "29. 感到孤独", "抑郁"));
        questions.add(new SCL90QuestionDTO(30, "30. 感到苦闷", "抑郁"));
        questions.add(new SCL90QuestionDTO(31, "31. 过分担忧", "抑郁"));
        questions.add(new SCL90QuestionDTO(32, "32. 对事物不感兴趣", "抑郁"));
        questions.add(new SCL90QuestionDTO(33, "33. 感到害怕", "焦虑"));
        questions.add(new SCL90QuestionDTO(34, "34. 我的感情容易受到伤害", "人际关系敏感"));
        questions.add(new SCL90QuestionDTO(35, "35. 旁人能知道您的私下想法", "精神病性"));
        questions.add(new SCL90QuestionDTO(36, "36. 感到别人不理解您不同情您", "人际关系敏感"));
        questions.add(new SCL90QuestionDTO(37, "37. 感到人们对你不友好，不喜欢你", "人际关系敏感"));
        questions.add(new SCL90QuestionDTO(38, "38. 做事必须做得很慢以保证做得正确", "强迫症状"));
        questions.add(new SCL90QuestionDTO(39, "39. 心跳得很厉害", "焦虑"));
        questions.add(new SCL90QuestionDTO(40, "40. 恶心或胃部不舒服", "躯体化"));
        questions.add(new SCL90QuestionDTO(41, "41. 感到比不上他人", "人际关系敏感"));
        questions.add(new SCL90QuestionDTO(42, "42. 肌肉酸痛", "躯体化"));
        questions.add(new SCL90QuestionDTO(43, "43. 感到有人在监视您谈论您", "偏执"));
        questions.add(new SCL90QuestionDTO(44, "44. 难以入睡", "其他"));
        questions.add(new SCL90QuestionDTO(45, "45. 做事必须反复检查", "强迫症状"));
        questions.add(new SCL90QuestionDTO(46, "46. 难以作出决定", "强迫症状"));
        questions.add(new SCL90QuestionDTO(47, "47. 怕乘电车、公共汽车、地铁或火车", "恐怖"));
        questions.add(new SCL90QuestionDTO(48, "48. 呼吸有困难", "躯体化"));
        questions.add(new SCL90QuestionDTO(49, "49. 一阵阵发冷或发热", "躯体化"));
        questions.add(new SCL90QuestionDTO(50, "50. 因为感到害怕而避开某些东西，场合或活动", "恐怖"));
        questions.add(new SCL90QuestionDTO(51, "51. 脑子变空了", "强迫症状"));
        questions.add(new SCL90QuestionDTO(52, "52. 身体发麻或刺痛", "躯体化"));
        questions.add(new SCL90QuestionDTO(53, "53. 喉咙有梗塞感", "躯体化"));
        questions.add(new SCL90QuestionDTO(54, "54. 感到对前途没有希望", "抑郁"));
        questions.add(new SCL90QuestionDTO(55, "55. 不能集中注意力", "强迫症状"));
        questions.add(new SCL90QuestionDTO(56, "56. 感到身体的某一部分软弱无力", "躯体化"));
        questions.add(new SCL90QuestionDTO(57, "57. 感到紧张或容易紧张", "焦虑"));
        questions.add(new SCL90QuestionDTO(58, "58. 感到手或脚发沉", "躯体化"));
        questions.add(new SCL90QuestionDTO(59, "59. 想到有关死亡的事", "其他"));
        questions.add(new SCL90QuestionDTO(60, "60. 吃得太多", "其他"));
        questions.add(new SCL90QuestionDTO(61, "61. 当别人看着您或谈论您时感到不自在", "人际关系敏感"));
        questions.add(new SCL90QuestionDTO(62, "62. 有一些不属于您自己的想法", "精神病性"));
        questions.add(new SCL90QuestionDTO(63, "63. 有想打人或伤害他人的冲动", "敌对"));
        questions.add(new SCL90QuestionDTO(64, "64. 醒得太早", "其他"));
        questions.add(new SCL90QuestionDTO(65, "65. 必须反复洗手、点数目或触摸某些东西", "强迫症状"));
        questions.add(new SCL90QuestionDTO(66, "66. 睡得不稳不深", "其他"));
        questions.add(new SCL90QuestionDTO(67, "67. 有想摔坏或破坏东西的冲动", "敌对"));
        questions.add(new SCL90QuestionDTO(68, "68. 有一些别人没有的想法或念头", "偏执"));
        questions.add(new SCL90QuestionDTO(69, "69. 感到对别人神经过敏", "人际关系敏感"));
        questions.add(new SCL90QuestionDTO(70, "70. 在商店或电影院等人多的地方感到不自在", "恐怖"));
        questions.add(new SCL90QuestionDTO(71, "71. 感到任何事情都很难做", "抑郁"));
        questions.add(new SCL90QuestionDTO(72, "72. 一阵阵恐惧或惊恐", "焦虑"));
        questions.add(new SCL90QuestionDTO(73, "73. 感到在公共场合吃东西很不舒服", "人际关系敏感"));
        questions.add(new SCL90QuestionDTO(74, "74. 经常与人争论", "敌对"));
        questions.add(new SCL90QuestionDTO(75, "75. 单独一人时神经很紧张", "恐怖"));
        questions.add(new SCL90QuestionDTO(76, "76. 别人对您的成绩没有作出恰当的评价", "偏执"));
        questions.add(new SCL90QuestionDTO(77, "77. 即使和别人在一起也感到孤单", "精神病性"));
        questions.add(new SCL90QuestionDTO(78, "78. 感到坐立不安心神不宁", "焦虑"));
        questions.add(new SCL90QuestionDTO(79, "79. 感到自己没有什么价值", "抑郁"));
        questions.add(new SCL90QuestionDTO(80, "80. 感到熟悉的东西变成陌生或不象是真的", "焦虑"));
        questions.add(new SCL90QuestionDTO(81, "81. 大叫或摔东西", "敌对"));
        questions.add(new SCL90QuestionDTO(82, "82. 害怕会在公共场合昏倒", "恐怖"));
        questions.add(new SCL90QuestionDTO(83, "83. 感到别人想占您的便宜", "偏执"));
        questions.add(new SCL90QuestionDTO(84, "84. 为一些有关\"性\"的想法而很苦恼", "精神病性"));
        questions.add(new SCL90QuestionDTO(85, "85. 认为应该因为自己的过错而受到惩罚", "精神病性"));
        questions.add(new SCL90QuestionDTO(86, "86. 感到要赶快把事情做完", "焦虑"));
        questions.add(new SCL90QuestionDTO(87, "87. 感到自己的身体有严重问题", "精神病性"));
        questions.add(new SCL90QuestionDTO(88, "88. 从未感到和其他人很亲近", "精神病性"));
        questions.add(new SCL90QuestionDTO(89, "89. 感到自己有罪", "其他"));
        questions.add(new SCL90QuestionDTO(90, "90. 感到自己的脑子有毛病", "精神病性"));
        
        return ResponseEntity.ok(questions);
    }
    
    @PostMapping("/results")
    public ResponseEntity<SCL90Result> saveResult(@RequestBody SCL90ResultDTO resultDTO) {
        SCL90Result savedResult = scl90Service.saveResult(resultDTO);
        return ResponseEntity.ok(savedResult);
    }
    
    @GetMapping("/results/{userId}")
    public ResponseEntity<SCL90Result> getResultByUserId(@PathVariable Long userId) {
        Optional<SCL90Result> resultOpt = scl90Service.getResultByUserId(userId);
        
        if (resultOpt.isPresent()) {
            return ResponseEntity.ok(resultOpt.get());
        }
        
        return ResponseEntity.notFound().build();
    }
    
    @DeleteMapping("/results/{userId}")
    public ResponseEntity<Void> deleteResult(@PathVariable Long userId) {
        scl90Service.deleteResultByUserId(userId);
        return ResponseEntity.ok().build();
    }
} 