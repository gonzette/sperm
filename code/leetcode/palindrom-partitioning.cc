#include <cstdio>
#include <cstdlib>
#include <string>
#include <vector>
#include <algorithm>
using namespace std;

class Solution {
public:
  vector< vector<string> > partition(string s) {
    // Start typing your C/C++ solution below
    // DO NOT write int main() function
    const int size = s.size();

    int* pcache = new int[size * size];
    memset(pcache, 0xff, sizeof(int) * size * size);
    for(int i = 0; i < s.size(); i++) {
      for(int j = 0; j < s.size(); j++) {
        preCalculatePalindrome(s, i, j, pcache);
      }
    }

    vector< vector<string> > vvs = partitionWithCache(s, 0, pcache);
    for(int i = 0; i < vvs.size(); i++) {
      reverse(vvs[i].begin(), vvs[i].end());
    }

    delete[] pcache;
    return vvs;
  }

  vector< vector<string> > partitionWithCache(const string& s, int offset, int pcache[]) {
    vector< vector<string> > vvs;
    if(isPalindrome(s, offset, s.size() - 1, pcache)) {
      vector<string> x;
      x.push_back(s.substr(offset));
      vvs.push_back(x);
    }
    for(int i = offset + 1; i < s.size(); i++) {
      if(isPalindrome(s, offset, i - 1, pcache)) {
        string sub = s.substr(offset, i - offset);
        vector< vector<string> > sub_vvs = partitionWithCache(s, i, pcache);
        for(int i = 0; i < sub_vvs.size(); i++) {
          sub_vvs[i].push_back(sub);
          vvs.push_back(sub_vvs[i]);
        }
      }
    }
    return vvs;
  }

  bool isPalindrome(const string& str, int s, int e, int pcache[]) {
    int index = s * str.size() + e;
    return pcache[index];
  }

  int preCalculatePalindrome(const string& str, int s, int e, int cache[]) {
    int index = s * str.size() + e;
    if(cache[index] != -1) {
      return cache[index] == 1;
    }
    int v = 1;
    if(s < e) {
      if(str[s] != str[e]) {
        v = 0;
      } else {
        v = preCalculatePalindrome(str, s + 1, e - 1, cache);
      }
    }
    cache[index] = v;
    return v;
  }

  void printVVS(const vector< vector<string> >& s) {
    for(int i = 0; i < s.size(); i++) {
      printf("[");
      for(int j = 0; j < s[i].size(); j++) {
        printf("%s ", s[i][j].c_str());
      }
      printf("]\n");
    }
  }
};

int main() {
  Solution s;
  vector< vector<string> > vvs = s.partition("aab");
  s.printVVS(vvs);
  return 0;
}
