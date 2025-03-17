'''
This file implements a sensitive word filter using the flashtext library.
It allows loading, checking, adding, and deleting sensitive words.
'''

from flashtext import KeywordProcessor
from typing import Optional, List

class SensitiveWordFilter:
    # Initialize the sensitive word filter
    def __init__(self):
        self.keyword_processor = KeywordProcessor(case_sensitive=False)
        self._load_sensitive_words()
    
    # Load the sensitive words library
    def _load_sensitive_words(self) -> None:
        try:
            with open("sensitive_words_lines.txt", "r", encoding="utf-8") as file:
                for line in file:
                    word = line.strip()
                    if word:
                        self.keyword_processor.add_keyword(word)
        except FileNotFoundError:
            print("Warning: Sensitive words file not found")
            # Create an empty file
            open("sensitive_words_lines.txt", "w", encoding="utf-8").close()

    # Check if the text contains sensitive words, return the first found
    def check_sensitive_words(self, text: str) -> Optional[str]:
        found_words = self.keyword_processor.extract_keywords(text)
        return found_words[0] if found_words else None

    # Find all sensitive words in the text
    def find_all_sensitive_words(self, text: str) -> List[str]:
        return self.keyword_processor.extract_keywords(text)

    # Add a new sensitive word to memory and file
    def add_sensitive_word(self, word: str) -> bool:
        word = word.strip()
        if not word:
            return False
            
        # Read existing words and remove duplicates
        with open("sensitive_words_lines.txt", "r", encoding="utf-8") as f:
            words = set(line.strip() for line in f if line.strip())
            
        if word in words:  # Return if the word already exists
            return False
            
        # Add to memory
        self.keyword_processor.add_keyword(word)
        
        # Add to file
        with open("sensitive_words_lines.txt", "a", encoding="utf-8") as f:
            f.write(f"\n{word}")
        return True

    # Delete a specified sensitive word from memory and file
    def delete_sensitive_word(self, word: str) -> bool:
        word = word.strip()
        if not word:
            return False
            
        # Read all non-empty lines
        with open("sensitive_words_lines.txt", "r", encoding="utf-8") as f:
            words = [line.strip() for line in f if line.strip()]
        
        if word not in words:
            return False
            
        # Remove from memory
        self.keyword_processor.remove_keyword(word)
        
        # Remove from file and rewrite
        words.remove(word)
        with open("sensitive_words_lines.txt", "w", encoding="utf-8") as f:
            f.write("\n".join(words))
        return True

if __name__ == "__main__":
    filter = SensitiveWordFilter()
    
    # Test adding sensitive words
    # print("Test adding sensitive words:")
    # print(filter.add_sensitive_word("敏感词1"))
    # print(filter.add_sensitive_word("敏感词1"))
    
    # Test deleting sensitive words
    # print("\nTest deleting sensitive words:")
    # print(filter.delete_sensitive_word("敏感词1"))
    # print(filter.delete_sensitive_word("不存在的词"))
    
    # Test text checking
    test_text = "这是一个测试文本"
    print("\nCheck text:", test_text)
    print("Found sensitive words:", filter.find_all_sensitive_words(test_text))