#include <unordered_map>

namespace indexer {
    // class TermScores
    // {  
    //     public:
    //         std::unordered_map<std::string, float> term_score_map;
    //        // float GetScore(std::string term);
    //         bool HasTerm(std::string term);
    //        // void UpdateScore(std::string term, float new_score);
    // };

    class TermFreqs
    {
        private:
            std::unordered_map<std::string, std::unordered_map<std::string, float>> doc_term_scores;
        public:
            float GetScore(std::string doc_id, std::string term);
         //   bool HasDoc(std::string doc_id);
            std::unordered_map<std::string, float> TermFreqs::TermScores(std::string doc_id);
            void UpdateScore(std::string doc_id, std::string term, float new_score);
    }; 
}