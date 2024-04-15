import asyncio
import aiohttp
import pymongo
import requests
from motor.motor_asyncio import AsyncIOMotorClient
import time
class CoroutineScrapeWorker:
    url = "https://apipc-xiaotuxian-front.itheima.net/home/category/head"

    def __init__(self):
        self.headers = {
            "User-Agent": "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/"}
        self.response = requests.get(url=self.url, headers=self.headers)
        self.head_result = self.response.json()
        self.client = AsyncIOMotorClient(host='localhost', port=27017)
        self.db = self.client.goods
        self.collection = self.db.car_accessories_coroutine

    def close(self):
        self.client.close()

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

    async def get_good_detail(self, good_id):
        url = "https://apipc-xiaotuxian-front.itheima.net/goods?id=" + str(good_id)
        async with aiohttp.ClientSession() as session:
            async with session.get(url,headers=self.headers)as response:
                result =await response.json()
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

    async def write_to_mongoDB(self,good_info):
        print(await self.collection.insert_one(good_info))


    def delete_from_mongoDB(self,aim_dict):
        print(self.collection.delete_one(aim_dict))

    def find_from_mongoDB(self,aim_dict):
        result = self.collection.find_one(aim_dict)
        print(type(result))
        print(result)

    async def single_coroutine_task(self,good_id):
        good_list_info=await self.get_good_detail(good_id)
        await self.write_to_mongoDB(good_list_info)

async def main():
    start_time=time.time()
    worker=CoroutineScrapeWorker()
    aim_id=worker.get_aim_type_id("数码","车载用品")
    good_ids=worker.get_good_ids(aim_id,100)
    tasks=[worker.single_coroutine_task(good_id)for good_id in good_ids]
    await asyncio.gather(*tasks)
    print(time.time()-start_time)
    worker.close()

if __name__=="__main__":
    asyncio.run(main())
