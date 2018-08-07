#include <unordered_map>

namespace indexer {

    class TermFreqs
    {
        private:
            std::unordered_map<std::string, std::unordered_map<std::string, float>> doc_term_scores_;
        public:
            float GetScore(const std::string& doc_id, const std::string& term);
            bool HasDoc(const std::string& doc_id) const;
            const std::unordered_map<std::string, float>& TermScores (const std::string& doc_id) const;
            void UpdateScore(const std::string& doc_id, const std::string& term, float new_score);
    }; 
}