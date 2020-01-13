<template xmlns:v-slot="http://www.w3.org/1999/XSL/Transform">
	<v-layout align-center justify-start column fill-height style="border: solid 0 blue">
		<v-container class="px-2 py-1">
			<v-layout class="mb-3">
				<v-card style="width: 100%">
					<v-card-title class="pb-0">
						<h3 class="ml-2">Профильный трафик</h3>
						<v-spacer></v-spacer>
						<v-btn color="primary" @click="createTraffic">
							Создать новый трафик
						</v-btn>
					</v-card-title>
					<v-data-table-custom
							:headers="headers"
							:items.sync="data"
							:loading="loadData"
							:total-items="total"
							:pagination.sync="pagination"
							:rows-per-page-items="pages"
							item-key="name"
							class="mx-4"
					>
						<template v-slot:items="props">
							<tr>
								<td><a @click="viewTraffic(props.item.id)">{{ props.item.id | noData}}</a></td>
								<td><a @click="viewTraffic(props.item.id)">{{ props.item.name | noData}}</a></td>
								<td><a @click="viewTraffic(props.item.id)">{{ props.item.count | noData}}</a></td>
								<td class="text-xs-right"><v-icon @click="deleteTraffic(props.item.id)" class="to-trash" color="primary">delete</v-icon></td>
							</tr>
						</template>

						<template v-slot:no-data v-if="!loadData">
							<v-alert :value="true" color="warning" icon="warning">
								Нет данных для отображения.Нажмите "Создать новый трафик"
							</v-alert>
						</template>
					</v-data-table-custom>
				</v-card>
			</v-layout>
		</v-container>
	</v-layout>

</template>

<script>
    export default {
        name: "TrafficList",

        data() {
            return {
                pagination: {
                    sortBy: 'id', 'descending': true,
                },
                pages: [25, 50, 100],
                total: 0,
                headers: [
                    {text: '№', value: 'id'},
                    {text: 'Наименования', value: 'title'},
                    {text: 'Количество записей', value: 'counter'},
                    {text: 'Действия', align: 'right', sortable: false},
                    //{text: 'Вид', value: 'type'},
                ],
                loadData: true,
                data: [],
				currentPage: {}

			}
		},

        filters: {
            noData(v) {
                return (v == null || v == '') ? '-' : v;
            }
        },

        watch: {
            pagination: {
                handler(v) {
                	this.currentPage = v;
                    this.nextPage(v.page - 1, v.rowsPerPage, v.sortBy, v.descending);
                },
                deep: true,
            }
        },


		methods:{
            createTraffic() {
                this.$axios.post(this.$urls.TRAFFIC).then(resp => {
                    return resp.data.id;
                }).then(id=>{
                    this.viewTraffic(id);
				});

            },

            viewTraffic(id) {
                this.$router.push({name: 'trafficinfo', params: {id: id}});
            },

			deleteTraffic(id) {
				let url = `${this.$urls.TRAFFIC}/${id}`;
				this.$axios.delete(url).then(() => {
					this.nextPage(this.currentPage.page - 1, this.currentPage.rowsPerPage, this.currentPage.sortBy, this.currentPage.descending);
				}).catch((e) => {
					console.log('error: ', e);
				});
			},

            nextPage(pageNumber, pageSize, sortingColumn, sortingDirection) {
                this.data = [];
                this.loadData = true;
                let params = {
                    pageSize: pageSize,
                    pageNumber: pageNumber,
                    sortingColumn: sortingColumn,
                    sortingDirection: this.sortTable(sortingDirection),
                };
                const config = () => ({params: params});
                this.$axios.get(this.$urls.TRAFFIC, config()).then(resp => {
                    this.data = resp.data.content;
                    this.total = resp.data.totalElements;
                    this.loadData = false;
                }).catch((error) => {
                    console.log('error: ', error);
                    this.loadData = false;
                });
            },

            sortTable(descending){
                if(descending!=null){
                    if (descending)
                        return 'DESC';
                    else return 'ASC'
                }else return descending;
            },
        }

    }
</script>

<style scoped>
	.to-trash{
		cursor: pointer;
		max-width: 21px;
	}
</style>