#include <string>
#include <set>
#include <unordered_set>
#include <cstring>
#include <vector>
#include <algorithm>

using namespace std;

typedef vector< vector<string> > VVS;
typedef vector<string> VS;

class Solution {
 public:
  int N;
  vector<string> res;
  int* dist;

  vector< vector<string> > findLadders(string start, string end, unordered_set<string> &dict) {
    // Start typing your C/C++ solution below
    // DO NOT write int main() function
    res.clear();
    res.push_back(start);
    for(unordered_set<string>::iterator it=dict.begin();it!=dict.end();++it) {
      res.push_back(*it);
    }
    res.push_back(end);
    N = res.size();
    
    // construct dist
    dist = new int [N * N];
    memset(dist,0,sizeof(int) * N * N);    
    calcDist();
    printDist();
    
    calcFloyd();
    printDist();
    VVS vvs = getDescription(0);
    //adjustDescription(vvs);
    
    return vvs;
  }

  int getIndex(int i,int j) {
    return i * N + j;
  }

  bool isAdjacent(int i,int j) {
    int diff = 0;
    for(int x = 0; x<res[i].size();x++) {
      if(res[i][x] != res[j][x]) {
        diff++;
      }
    }
    return diff <= 1;
  }

  void printDist() {
    printf("--------------------\n");
    for(int i=0;i<N;i++) {
      for(int j=0;j<N;j++) {
        printf("%2d ",dist[getIndex(i,j)]);
      }
      printf("\n");
    }
  }
  
  void calcDist() {
    for(int i=0;i<N;i++) {
      for(int j=i;j<N;j++) {        
        if(i == j) {
          dist[getIndex(i,j)] = 0;
        } else if(isAdjacent(i,j)) {        
          dist[getIndex(i,j)] = 1;
          dist[getIndex(j,i)] = 1;
        } else {
          dist[getIndex(i,j)] = -1;
          dist[getIndex(j,i)] = -1;
        }
      }
    }
  }

  void calcFloyd() {
    for(int k=0;k<N;k++) {
      for(int i=0;i<N;i++) {
        for(int j=0;j<N;j++) {
          if(dist[getIndex(i,k)] == -1 ||
             dist[getIndex(k,j)] == -1) {
            continue;
          }
          int d = dist[getIndex(i,k)] + dist[getIndex(k,j)];
          int id = getIndex(i,j);
          if(dist[id] == -1 || d < dist[id]) {
            dist[id] = d;
          }
        }
      }
    }
  }

  VVS getDescription(int f) {
    if(f == (N-1)) {
      VVS vvs;
      VS vs;
      vs.push_back(res[f]);
      vvs.push_back(vs);
      return vvs;
    } else {
      VVS vvs;
      for(int i=0;i<N;i++) {
        if(dist[getIndex(f,i)] == 1 && 
           dist[getIndex(i,N-1)] == (dist[getIndex(f,N-1)] - 1)) {
          //printf("f = %d, i = %d\n",f,i);
          VVS vvs2 = getDescription(i);
          for(int m = 0; m < vvs2.size(); m++) {
            VS vs;
            vs.push_back(res[f]);
            for(int x = 0; x < vvs2[m].size(); x++) {
              vs.push_back(vvs2[m][x]);
            }
            vvs.push_back(vs);
          }
        }
      }    
      return vvs;
    }
  }

  void adjustDescription(VVS& vvs) {
    for(int i=0;i<vvs.size();i++) {
      reverse(vvs[i].begin(),vvs[i].end());
    }
  }
  
  void printVVS(const VVS& vvs) {
    for(int i=0;i<vvs.size();i++) {
      printf("[");
      for(int j=0;j<vvs[i].size();j++) {
        printf("%s ",vvs[i][j].c_str());
      }
      printf("]\n");
    }
  }
};

int main() {
  Solution sol;
  
  unordered_set<string> ss;
  const char* dict[] = {
    "hot","dot","dog","lot","log", NULL,
  };
  for(int i=0;dict[i];i++) {
    ss.insert(string(dict[i]));
  }

  VVS vvs = sol.findLadders("hit","cog",ss);
  sol.printVVS(vvs);
  return 0;
}
