import pandas as pd
import random
import matplotlib.pyplot as plt
import seaborn as sns
import numpy as np
import pickle

with open("../result/train_test.pkl", "rb") as f:
    train, test = pickle.load(f)

transition_df = pd.read_csv("../result/item_transition_top20.csv")

all_recs = {}
for user, basket in train.items():
    if not basket:
        continue
    last_item = basket[-1]
    recs = transition_df.loc[transition_df["source_product_id"] == last_item].sort_values(
        "score", ascending=False)["target_product_id"].astype(int).tolist()
    all_recs[user] = recs[:20]


def ndcg_at_k(recommended, truth, k):
    recs = recommended[:k]
    dcg = sum([1/np.log2(i+1)
              for i, item in enumerate(recs, start=1) if item in truth])
    idcg = sum([1/np.log2(i+1) for i in range(1, min(len(truth), k)+1)])
    return dcg/idcg if idcg > 0 else 0.0


def mrr_at_k(recommended, truth, k):
    recs = recommended[:k]
    for i, item in enumerate(recs, start=1):
        if item in truth:
            return 1.0/i
    return 0.0


def ap_at_k(recommended, truth, k):
    recs = recommended[:k]
    hits = 0
    score = 0.0
    for i, item in enumerate(recs, start=1):
        if item in truth:
            hits += 1
            score += hits/i
    return score/len(truth) if truth else 0.0


def map_at_k(all_recs, test, k):
    scores = [ap_at_k(all_recs[u], test.get(u, []), k)
              for u in all_recs if test.get(u, [])]
    return sum(scores)/len(scores) if scores else 0.0


def eval_metrics(all_recs, test, k):
    ndcg = []
    mrr = []
    for u in all_recs:
        truth = test.get(u, [])
        if truth:
            ndcg.append(ndcg_at_k(all_recs[u], truth, k))
            mrr.append(mrr_at_k(all_recs[u], truth, k))
    return (sum(ndcg)/len(ndcg) if ndcg else 0.0,
            sum(mrr)/len(mrr) if mrr else 0.0)


random.seed(42)
all_items = pd.concat([pd.Series(v) for v in train.values()]).unique().tolist()
random_recs = {user: random.sample(
    all_items, min(20, len(all_items))) for user in train}
pop_items = pd.Series(all_items).value_counts().index.tolist()[:20]
pop_recs = {user: pop_items for user in train}

ndcg_t, mrr_t = eval_metrics(all_recs, test, 20)
ndcg_r, mrr_r = eval_metrics(random_recs, test, 20)
ndcg_p, mrr_p = eval_metrics(pop_recs, test, 20)

map_t = map_at_k(all_recs, test, 20)
map_r = map_at_k(random_recs, test, 20)
map_p = map_at_k(pop_recs, test, 20)

results = pd.DataFrame({
    "method": ["Transition", "Random", "Popularity"],
    "MAP": [map_t, map_r, map_p],
    "NDCG": [ndcg_t, ndcg_r, ndcg_p],
    "MRR": [mrr_t, mrr_r, mrr_p]
})

results.to_csv("../result/comparison_metrics_multi.csv",
               index=False, encoding="utf-8-sig")

plt.figure(figsize=(10, 6))
metrics = results.melt(id_vars="method", var_name="metric", value_name="score")
sns.barplot(x="metric", y="score", hue="method", data=metrics)
plt.title("Multi-label Metrics Comparison (MAP, NDCG, MRR)")
plt.savefig("../graph/metrics_comparison_multi.png")
plt.close()

plt.figure(figsize=(8, 6))
sns.heatmap(results.set_index("method"), annot=True, cmap="Blues", fmt=".4f")
plt.title("Multi-label Metrics Heatmap (MAP, NDCG, MRR)")
plt.savefig("../graph/metrics_heatmap_multi.png")
plt.close()
