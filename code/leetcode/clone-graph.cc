/**
 * Definition for undirected graph.
 * struct UndirectedGraphNode {
 *     int label;
 *     vector<UndirectedGraphNode *> neighbors;
 *     UndirectedGraphNode(int x) : label(x) {};
 * };
 */

#include <cstdio>
#include <vector>
#include <map>
using namespace std;

struct UndirectedGraphNode {
  int label;
  vector<UndirectedGraphNode*> neighbors;
  UndirectedGraphNode(int x) : label(x) {};
};

class Solution {
public:
  UndirectedGraphNode* cloneGraph(UndirectedGraphNode* node) {
    // Note: The Solution object is instantiated only once and is reused by each test case.
    if(node == NULL) {
      return NULL;
    }
    map<int, UndirectedGraphNode*> cache;
    return cloneGraphWithCache(node, cache);
  }

  UndirectedGraphNode* cloneGraphWithCache(UndirectedGraphNode* node, map<int, UndirectedGraphNode*>& cache) {
    UndirectedGraphNode*& value = cache[node->label];
    if(value != NULL) {
      return value;
    }
    UndirectedGraphNode* copy = new UndirectedGraphNode(node->label);
    value = copy; // put into cache.
    for(int i = 0; i < node->neighbors.size(); i++) {
      copy->neighbors.push_back(cloneGraphWithCache(node->neighbors[i], cache));
    }
    return copy;
  }
};

int main() {
  return 0;
}
