//header for
#include "tinyxml2.h" 
#include "porter2_stemmer.h"
#include "index.h"
#include <iostream> 
#include <string>
#include <vector>
#include <algorithm>
#include <regex>
#include <unordered_map>
#include <fstream>
// #include <stdio.h>
//mediawiki xml:lang="en"
namespace indexer {

    float TermFreqs::GetScore(const std::string& doc_id, const std::string& term) {
        if (doc_term_scores_.find(doc_id) == doc_term_scores_.end()){
            return -1.f;
        }
        return doc_term_scores_[doc_id][term];
    }

    bool TermFreqs::HasDoc(const std::string& doc_id) const {

        auto iter = doc_term_scores_.find(doc_id);
        return !(iter == doc_term_scores_.end());
    }

    const std::unordered_map<std::string, float>& TermFreqs::TermScores(const std::string& doc_id) const {
        //const auto ref 
        auto iter = doc_term_scores_.find(doc_id);
        // check 
        //returning instead of throwing for errors
        //unique ptr
        if (iter == doc_term_scores_.end()) {
            //this case shouldn't be hit
            std::cerr << "looking up doc_id that does not exist!" << std::endl;
        }
        return iter->second;
    }

    void TermFreqs::UpdateScore(const std::string& doc_id, const std::string& term, float new_score) {
        doc_term_scores_[doc_id][term] = new_score;
    }

    void WriteScores(const std::unique_ptr<TermFreqs>& tf, const std::unique_ptr<std::unordered_map<std::string, float>>& idf, 
                     std::unordered_map<std::string, std::string> id_title) {
        //write term and associated score (tf * idf) under each page
        //TODO: add in logic to write id to title to file
        std::cout << "write start" << std::endl;
        std::ofstream score_file;
        score_file.open("id_scores.txt");
        for (std::pair<std::string, std::string> docid_title : id_title) {
            const std::string doc_id = docid_title.first;
            score_file << doc_id << std::endl;
            // iterate through docs for this term
            //huh???
            //const auto & to not make a copy
            //-Werror -> compiler prints error turns all warnings to all
            // -Wall -> all warnings
            //maybe look at lua??
            // drracket-> rts
            //underscore at end of fields in classes
            if (!(*tf).HasDoc(doc_id)) {
                continue;
            }
            const std::unordered_map<std::string, float>& term_tfs = (*tf).TermScores(doc_id);
            for (std::pair<std::string, float> term_tf : term_tfs) {
                score_file << term_tf.first << " ";
                float score = term_tf.second * (*idf)[term_tf.first];
                score_file << score << std::endl;
            }
        }
        score_file.close();
    }

    std::vector<std::string> RegexSplit(const std::string& text, const std::regex& my_regex) {
        std::vector<std::string> tokens;
        std::sregex_token_iterator iter(text.begin(), text.end(), my_regex);
        std::sregex_token_iterator end;
        while (iter != end)  {
            tokens.push_back(*iter);
            iter++;
        }
        return tokens;
    }

    std::unique_ptr<TermFreqs> NormalizeTf(int max_token_count, const std::string& doc_id, std::unique_ptr<TermFreqs> tf) {
        std::unordered_map<std::string, float> term_scores = (*tf).TermScores(doc_id);
        for (std::pair<std::string, float> token_tf : term_scores) {
            (*tf).UpdateScore(doc_id, token_tf.first, token_tf.second/max_token_count);
        }
        return tf;
    }

    struct Scores {
        std::unique_ptr<TermFreqs> tf;
        std::unique_ptr<std::unordered_map<std::string, float>> idf;
    };

    Scores ScorePage(const std::string& page_id, std::vector<std::string> tokens, std::unique_ptr<TermFreqs> tf, 
                   std::unique_ptr<std::unordered_map<std::string, float>> idf) {
        int max_term_ct = 1;
        //scoring
        *idf;
        
        for (std::string token: tokens) {
            
            *idf;
            token;
            std::cout << "oops";
            (*idf).emplace(std::make_pair(token, 0.f));
            std::cout << "lolempl" << std::endl;
            // if ((*idf).find(token) == (*idf).end()) {
            //     (*idf)[token] = 0.0;
            // }
            (*idf);
            std::cout << "lolttttrgradgempl" << std::endl;
            //idf maps token to score
            //tf maps term to token dict
            (*idf)[token] = (*idf)[token] + 1.f;
            //(*idf)[token] = 1.f;
            
            if (token.at(0) != '[') {
                Porter2Stemmer::trim(token);
                Porter2Stemmer::stem(token);
            }
            float curr_score = (*tf).GetScore(page_id, token);
            float updated_score = curr_score + 1;
            if (curr_score > -1.f) {
                (*tf).UpdateScore(page_id, token, updated_score);
            } else {
                (*tf).UpdateScore(page_id, token, 1.f);
                    max_term_ct = std::max(max_term_ct, (int)updated_score);
            }
            std::cout << "endl" << std::endl;
        }

        tf = NormalizeTf(max_term_ct, page_id, std::move(tf));
        Scores scores = {std::move(tf), std::move(idf)};
        std::cout << "postnorm" << std::endl;
        return scores;
        
    }

    //tf is {#term i occurrences in doc j}/{max # times any word occurs in doc j}
    //idf is log({# docs in corpus}/{# of docs where term i occurs})
    Scores ParseAndScore(const char* corpus, std::unique_ptr<TermFreqs> tf, std::unique_ptr<std::unordered_map<std::string, float>> idf, 
                         std::unordered_map<std::string, std::string>* id_title) {
        tinyxml2::XMLDocument wiki;
        wiki.LoadFile(corpus);
        if (wiki.Error()) {
        	std::cerr << wiki.Error() << std::endl;
        }
        tinyxml2::XMLElement* DOM_root = wiki.FirstChildElement("xml");
        tinyxml2::XMLElement* curr_page = DOM_root->FirstChildElement("page");
        int page_count = 0;
        while (curr_page != nullptr) {
            page_count++;
            std::string page_id(curr_page->FirstChildElement("id")->GetText());
            (*id_title)[page_id] = curr_page->FirstChildElement("title")->GetText();
            if (curr_page->FirstChildElement("text")->GetText() == 0) {
                curr_page = curr_page->NextSiblingElement("page");
                continue;
            }
            std::string page_text(curr_page->FirstChildElement("text")->GetText());
            std::regex regex("""\\\[\\[[^\\[]+?\\]\\]|[\\w_]+'[\\w_]+|[\\w_]+""");
            std::vector<std::string> tokens = RegexSplit(page_text, regex);
            //give ownership 
            
            Scores scores = ScorePage(page_id, tokens, std::move(tf), std::move(idf));
            //take back ownership
            tf = std::move(scores.tf);
            idf = std::move(scores.idf);
            curr_page = curr_page->NextSiblingElement("page");
        }
        for (std::pair<std::string, float> id_score : *idf) {
            (*idf)[id_score.first] = log(page_count/id_score.second);
        }
        Scores scores = {std::move(tf), std::move(idf)};
        return scores;
    }

    void Index(const char* corpus_name) {
        //possibly make a separate class for this nested dict structure later?
        std::unique_ptr<TermFreqs> tf(new TermFreqs());
        std::unique_ptr<std::unordered_map<std::string, float>> idf(new std::unordered_map<std::string, float>);
        std::unordered_map<std::string, std::string> id_title;
        //id to tokens
        //(*tf).UpdateScore(std::string("hi mom"), std::string("hi dad"), 1.f);

        //std::unique_ptr<TermFreqs> my_move = std::move(tf);
        //std::cout << (*my_move).GetScore(std::string("hi mom"), std::string("hi dad")) << std::endl;
        Scores scores = ParseAndScore(corpus_name, std::move(tf), std::move(idf), &id_title);
        tf = std::move(scores.tf);
        idf = std::move(scores.idf);
        WriteScores(tf, idf, id_title);
    }

}

int main(int argc, char** argv) {
    if (argc < 2) {
        std::cerr << "Need to specify a corpus file name!" << std::endl;
    }
    const char * corpus_name = argv[1];
    indexer::Index(corpus_name);
    return 0;
}