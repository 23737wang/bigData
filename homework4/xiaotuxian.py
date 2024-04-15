import requests
import queue
import pymongo
from concurrent.futures import ThreadPoolExecutor
from concurrent.futures import wait
import time

class ScrapeWorker:
    url = "https://apipc-xiaotuxian-front.itheima.net/home/category/head"

    def __init__(self):
        self.headers = {
            "User-Agent": "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/"}
        self.response = requests.get(url=self.url, headers=self.headers)
        self.head_result = self.response.json()
        self.client = pymongo.MongoClient(host='localhost', port=27017)
        self.db = self.client.goods
        self.collection = self.db.car_accessories_multiple_thread

    def create_index(self):
        self.collection.create_index("price")

    def delete_index(self):
        self.collection.drop_index("name")
        self.collection.drop_index("desc")
        self.collection.drop_index("price")
    def get_aim_type_id(self, head_name, child_name):
        result_list = self.head_result['result']
        # 匹配head_name
        for result in result_list:
            if result['name'] == head_name:
                # 匹配child_name
                for child in result['children']:
                    if child['name'] == child_name:
                        return child['id']

    def get_good_ids(self, aim_id, good_num):
        url = "https://apipc-xiaotuxian-front.itheima.net/category/sub/filter?id=" + str(aim_id)
        response = requests.get(url=url, headers=self.headers)
        result = response.json()
        goods_list = result['result']['goods']
        goods_id = []
        for good in goods_list:
            good_id = good['id']
            goods_id.append(good_id)
            good_num -= 1
            if good_num == 0:
                break
        if good_num > 0:
            temporary = "https://apipc-xiaotuxian-front.itheima.net/category/goods/temporary"
            pages = good_num // 20 + 1
            for i in range(1, pages + 1):
                data = {
                    "page": i,
                    "pageSize": 20,
                    "categoryId": "109243036"
                }
                temporary_response = requests.post(url=temporary, headers=self.headers, json=data)
                temporary_result = temporary_response.json()
                print(temporary_result)
                goods_list = temporary_result['result']['items']
                for good in goods_list:
                    good_id = good['id']
                    goods_id.append(good_id)
                    good_num -= 1
                    if good_num == 0:
                        break
            if good_num > 0:
                print("商品数量不足")
        return goods_id

    def get_good_detail(self, good_id):
        url = "https://apipc-xiaotuxian-front.itheima.net/goods?id=" + str(good_id)
        response = requests.get(url=url, headers=self.headers)
        result = response.json()
        good_list_info = {}
        #获取列表页的数据
        good_list_info["id"]=result['result']['id']
        good_list_info['name'] = result['result']['name']
        good_list_info['price'] = result['result']['price']
        good_list_info['desc'] = result['result']['desc']
        good_list_info['images'] = []
        for image in result['result']['mainPictures']:
            good_list_info['images'].append(image)
        #获取详情页的数据
        detail_info = result['result']['details']["properties"]
        detail = {}
        for i in detail_info:
            detail[i['name']] = i['value']
        good_list_info['detail'] = detail
        return good_list_info

    def write_to_mongoDB(self,good_info):
        print(self.collection.insert_one(good_info))


    def delete_from_mongoDB(self,aim_dict):
        print(self.collection.delete_one(aim_dict))

    def find_from_mongoDB(self,aim_dict):
        results = self.collection.find(aim_dict)
        return list(results)


    def task_entry(self,head_name,child_name,goods_num):
        aim_type_id=self.get_aim_type_id(head_name,child_name)
        good_ids=self.get_good_ids(aim_type_id,goods_num)
        num=0
        for good_id in good_ids:
            good_detail=self.get_good_detail(good_id)
            self.write_to_mongoDB(good_detail)
            num+=1
        print(num)

    def create_queue(self,head_name,child_name,good_num):
        self.good_id_queue = queue.Queue()
        good_ids = self.get_good_ids(self.get_aim_type_id(head_name, child_name), good_num)
        for good_id in good_ids:
            self.good_id_queue.put(good_id)

    def single_thread_task(self):
        while True:
            try:
                good_detail=self.get_good_detail(self.good_id_queue.get(timeout=1))
                self.write_to_mongoDB(good_detail)
            except self.good_id_queue.Empty:
                break


def main():

    worker = ScrapeWorker()

    start_time = time.time()
    for i in range(5000):
        price=0.1*i
        rounded_price = round(price, 2)  # 保留两位小数
        query_price = {"price": "{:.2f}".format(rounded_price)}
        result=worker.find_from_mongoDB(query_price)
        if result:print(result)
    print(time.time() - start_time, "秒")
    #worker.task_entry("数码","车载用品",100)
    '''worker.create_queue("数码","车载用品",100)
    with ThreadPoolExecutor(10) as t:
        futures=[t.submit(worker.single_thread_task) for _ in range(50)]
        wait(futures)
    end_time=time.time()
    print(end_time-start_time,"秒")'''



main()