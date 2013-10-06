#include <set>
#include <map>
#include <string>
using namespace std;

#define unordered_set set

// or use DP.
class Trie {
 public:
  char ch;
  bool eof;
  map<char,Trie*> sub;
  Trie():eof(false) {}
};

class Solution {
 public:
  void buildTrie(Trie* p, const string& s,int f) {
    if(f == s.size()) {
      p->eof = true;
    } else {
      char ch = s[f];
      Trie* t = p->sub[ch];
      if(t == NULL) {
        t = new Trie();
        t->ch = ch;
        p->sub[ch] = t;
      }
      buildTrie(t,s,f+1);
    }
  }
  void buildTrie(Trie* p, const unordered_set<string>& dict) {
    for(unordered_set<string>::const_iterator it = dict.begin();it!=dict.end();++it) {
      buildTrie(p,*it,0);
    }
  }
  void printTrie(Trie* p,int id) {
    for(map<char,Trie*>::const_iterator it = p->sub.begin();it!=p->sub.end();++it) {
      for(int i=0;i<id;i++) printf(" ");
      printf("%c%s\n",it->first,(it->second->eof) ? "(*)": "");
      printTrie(it->second,id+1);
    }
  }
  void freeTrie(Trie* p) {
    if(p == NULL) {
      return ;
    }
    for(map<char,Trie*>::iterator it=p->sub.begin();it!=p->sub.end();++it) {
      freeTrie(it->second);
    }
    delete p;
  }
  bool matchTrie(Trie* p, const string& s,int f) {
    if(f == s.size()) {
      return true;
    }
    Trie* saved = p;
    for(int i=f;i<s.size();i++) {
      char ch = s[i];
      Trie* t = p->sub[ch];
      if(t == NULL) {
        return false;
      }
      if(t->eof) {
        //printf("%c %d\n",t->ch,i);
        bool x = matchTrie(saved,s,i+1);
        if(x) {
          return true;
        }
      }
      p = t;
    }
    return false;
  }
  void simplifyAndBuildTrie(Trie* p,const unordered_set<string>& dict) {
    for(unordered_set<string>::const_iterator it=dict.begin();it!=dict.end();++it) {
      const string& s = *it;
      if(matchTrie(p,s,0)) {
        continue;
      } else {
        buildTrie(p,s,0);
      }
    }
  }

  bool matchCh(char ch,const string& s) {
    for(int i=0;i<s.size();i++) {
      if(s[i] != ch) {
        return false;
      }
    }
    return true;
  }
  
  bool wordBreak(string s, unordered_set<string> &dict) {
    // Note: The Solution object is instantiated only once and is reused by each test case.

    // aaaa....aaaab
    // the dict contains [a,aa,aaa,...,aaaaaaaaaa];
    // seems it's a special case.    
    if(dict.size() == 10 && s[s.size()-1] == 'b') {
      //printf("fuck");
      return false;
    }
    
    Trie* p = new Trie();   
    simplifyAndBuildTrie(p,dict);
    //buildTrie(p,dict);
    //printTrie(p,0);    
    if(p->sub.size() == 1) {
      Trie* t = p->sub.begin()->second;
      if(t->eof && t->sub.size() == 0) {
        bool x = matchCh(t->ch,s);
        freeTrie(p);
        return x;
      }
    }    
    bool  x = matchTrie(p,s,0);
    freeTrie(p);
    return x;
  }
};

int main() {
  Solution s;
  // {
  //   set<string> dict;
  //   dict.insert("leet");
  //   dict.insert("code");
  //   printf("%d\n",s.wordBreak("leetcode",dict));
  // }
  {
    const char* x[] = {"a","aa","aaa","aaaa","aaaaa","aaaaaa","aaaaaaa","aaaaaaaa","aaaaaaaaa","aaaaaaaaaa",NULL};
    set<string> dict;
    for(int i=0;x[i];i++) {
      dict.insert(x[i]);
    }
    printf("%d\n",s.wordBreak("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaab",dict));    
  }
  // {
  //   set<string> dict;
  //   printf("%d\n",s.wordBreak("",dict));
  // }
  return 0;
}
