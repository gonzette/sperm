#include <string>
#include <set>
//#include <unordered_set>
#define unordered_set set
#include <cstring>
#include <vector>
#include <algorithm>
#include <queue>

using namespace std;

typedef vector< vector<string> > VVS;
typedef vector<string> VS;

class Solution {
public:
  int N;
  vector<string> res;
  vector<int>* graph;

  int ladderLength(string start, string end, unordered_set<string>& dict) {
    // Start typing your C/C++ solution below
    // DO NOT write int main() function
    res.clear();
    res.push_back(start);
    for(unordered_set<string>::iterator it = dict.begin(); it != dict.end(); ++it) {
      res.push_back(*it);
    }
    res.push_back(end);
    N = res.size();
    //printf("N = %d\n",N);

    graph = new vector<int> [N];
    makeGraph();

    int code = getMinDist();
    //int code = 0;

    delete[] graph;
    return code;
  }

  bool isAdjacent(int i, int j) {
    int diff = 0;
    for(int x = 0; x < res[i].size(); x++) {
      if(res[i][x] != res[j][x]) {
        diff++;
        if(diff >= 2) {
          return false;
        }
      }
    }
    return true;
  }

  void makeGraph() {
    for(int i = 0; i < N; i++) {
      for(int j = i + 1; j < N; j++) {
        if(isAdjacent(i, j)) {
          graph[j].push_back(i);
          graph[i].push_back(j);
        }
      }
    }
  }

  // NOTE(dirlt):not think through.
  int getMinDist() {
    char* mask = new char[N];
    queue<int> Q1;
    queue<int> Q2;
    Q1.push(0);
    mask[0] = 1;
    Q2.push(0);
    while(!Q1.empty()) {
      int n = Q1.front();
      Q1.pop();
      int d = Q2.front();
      Q2.pop();
      if(n == (N - 1)) {
        delete[] mask;
        return d + 1;
      }
      for(int i = 0; i < graph[n].size(); i++) {
        int v = graph[n][i];
        if(mask[v] == 0) {
          mask[v] = 1;
          Q1.push(v);
          Q2.push(d + 1);
        }
      }
    }
  }
};

int main() {
  Solution sol;
  {
    unordered_set<string> ss;
    const char* dict[] = {
      "hot", "dot", "dog", "lot", "log", NULL,
    };
    for(int i = 0; dict[i]; i++) {
      ss.insert(string(dict[i]));
    }
    printf("%d\n", sol.ladderLength("hit", "cog", ss));
  }
  return 0;
}
