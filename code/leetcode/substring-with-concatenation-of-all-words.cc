#include <string>
#include <vector>
#include <set>
#include <map>
#include <cstdio>
using namespace std;

class Solution {
public:
  vector<int> findSubstring(string S, vector<string>& L) {
    // Note: The Solution object is instantiated only once and is reused by each test case.
    int n = L.size();
    int m = L[0].size();
    map<string, int> dict;
    for(int i = 0; i < n; i++) {
      dict[L[i]] = i;
    }
    map<int, int> base_st;
    for(int i = 0; i < n; i++) {
      base_st[dict[L[i]]]++;
    }
    vector<int> res;
    for(int i = 0; i < m; i++) {
      map<int, int> st;
      for(int j = i; j < S.size(); j += m) {
        int ev = j - n * m;
        if(ev >= 0) {
          // remove S[ev]
          string ev_s = S.substr(ev, m);
          int ev_idx = getIndex(dict, ev_s);
          if(ev_idx != -1) {
            del_s(st, ev_idx);
          }
        }
        string j_s = S.substr(j, m);
        //printf("j_s = %s\n",j_s.c_str());
        int j_idx = getIndex(dict, j_s);
        if(j_idx != -1) {
          add_s(st, j_idx);
        }
        // TODO(dirlt):condition.
        //if(ok_s(st,n)) {
        if(ok_s(st, base_st)) {
          int sv = j - (n - 1) * m;
          res.push_back(sv);
        }
      }
    }
    return res;
  }
  void add_s(map<int, int>& s, int idx) {
    s[idx] += 1;
  }
  void del_s(map<int, int>& s, int idx) {
    s[idx] -= 1;
    if(s[idx] == 0) {
      s.erase(idx);
    }
  }
  // bool ok_s(map<int,int>& s,int n) {
  //   return s.size() == n;
  // }
  bool ok_s(map<int, int>& s, map<int, int>& e) {
    return s == e;
  }
  int getIndex(const map<string, int>& dict, const string& s) {
    map<string, int>::const_iterator it = dict.find(s);
    if(it == dict.end()) {
      return -1;
    }
    return it->second;
  }
  void pp(string S, vector<string>& L) {
    vector<int> res = findSubstring(S, L);
    printf("[");
    for(int i = 0; i < res.size(); i++) {
      printf("%d ", res[i]);
    }
    printf("]\n");
  }
};

int main() {
  Solution s;
  {
    vector<string> L;
    L.push_back("foo");
    L.push_back("bar");
    s.pp("barfoothefoobarman", L);
  }
  {
    vector<string> L;
    L.push_back("fooo");
    L.push_back("barr");
    L.push_back("wing");
    L.push_back("ding");
    L.push_back("wing");
    s.pp("lingmindraboofooowingdingbarrwingmonkeypoundcake", L);
  }
  return 0;
}
