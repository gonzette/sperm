#include <set>
#include <map>
#include <string>
#include <vector>
using namespace std;

#define unordered_set set

class Solution1 {
public:
  char* dp;
  int N;
  bool wordBreak(string s, unordered_set<string>& dict) {
    // Note: The Solution object is instantiated only once and is reused by each test case.
    const int n = s.size();
    if(n == 0) {
      return true;
    }
    N = n;
    dp = new char[n * n];
    memset(dp, 0xff, sizeof(char)*n * n);
    bool x = wb(s, 0, n - 1, dict);
    delete[] dp;
    return x;
  }
  int getIndex(int i, int j) {
    return i * N + j;
  }
  bool wb(const string& s, int f, int e, unordered_set<string>& dict) {
    if(f > e) {
      return true;
    }
    int index = getIndex(f, e);
    if(dp[index] != -1) {
      return dp[index] == 1;
    }
    string sub = s.substr(f, e - f + 1);
    bool res = false;
    if(dict.find(sub) == dict.end()) {
      for(int i = f + 1; i <= e; i++) {
        // [f,i-1],[i,e]
        bool b1 = wb(s, f, i - 1, dict);
        bool b2 = wb(s, i, e, dict);
        if(b1 && b2) {
          res = true;
          break;
        }
      }
    } else {
      res = true;
    }
    dp[index] = res ? 1 : 0;
    return res;
  }
};

class Trie {
public:
  char ch;
  bool eof;
  map<char, Trie*> sub;
  Trie(): eof(false) {}
};

class Solution {
public:
  void buildTrie(Trie* p, const string& s, int f) {
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
      buildTrie(t, s, f + 1);
    }
  }
  void buildTrie(Trie* p, const unordered_set<string>& dict) {
    for(unordered_set<string>::const_iterator it = dict.begin(); it != dict.end(); ++it) {
      buildTrie(p, *it, 0);
    }
  }
  void printTrie(Trie* p, int id) {
    for(map<char, Trie*>::const_iterator it = p->sub.begin(); it != p->sub.end(); ++it) {
      for(int i = 0; i < id; i++) printf(" ");
      printf("%c%s\n", it->first, (it->second->eof) ? "(*)" : "");
      printTrie(it->second, id + 1);
    }
  }
  void freeTrie(Trie* p) {
    if(p == NULL) {
      return ;
    }
    for(map<char, Trie*>::iterator it = p->sub.begin(); it != p->sub.end(); ++it) {
      freeTrie(it->second);
    }
    delete p;
  }
  void matchTrie(Trie* p, const string& s, int f, vector<int>& idx, vector<string>& vs) {
    if(f == s.size()) {
      int x = 0;
      string xs;
      for(int i = 0; i < idx.size(); i++) {
        int y = idx[i];
        string t = s.substr(x, y - x);
        if(x != 0) {
          xs += ' ';
        }
        xs += t;
        x = y;
      }
      vs.push_back(xs);
    }
    Trie* saved = p;
    for(int i = f; i < s.size(); i++) {
      char ch = s[i];
      Trie* t = p->sub[ch];
      if(t == NULL) {
        break;
      }
      if(t->eof) {
        //printf("%c %d\n",t->ch,i);
        idx.push_back(i + 1);
        matchTrie(saved, s, i + 1, idx, vs);
        idx.pop_back();
      }
      p = t;
    }
  }
  void printVS(const vector<string>& vs) {
    printf("[");
    for(int i = 0; i < vs.size(); i++) {
      printf("%s,", vs[i].c_str());
    }
    printf("]\n");
  }
  vector<string> wordBreak(string s, unordered_set<string>& dict) {
    // Note: The Solution object is instantiated only once and is reused by each test case.
    vector<string> vs;
    Solution1 s1;
    if(!s1.wordBreak(s, dict)) {
      return vs;
    }
    Trie* p = new Trie();
    buildTrie(p, dict);
    //printTrie(p,0);
    vector<int> idx;
    matchTrie(p, s, 0, idx, vs);
    freeTrie(p);
    return vs;
  }
};

int main() {
  Solution s;
  {
    set<string> dict;
    const char* x[] = {"cat", "cats", "and", "sand", "dog", NULL};
    for(int i = 0; x[i]; i++) dict.insert(x[i]);
    vector<string> res = s.wordBreak("catsanddog", dict);
    s.printVS(res);
  }
  return 0;
}
