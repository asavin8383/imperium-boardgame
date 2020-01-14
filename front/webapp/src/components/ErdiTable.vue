<template xmlns:v-slot="http://www.w3.org/1999/XSL/Transform">
	<v-layout column>
		<v-layout row class="mr-3">
			<v-spacer></v-spacer>
			<v-btn v-if="full" class="icon-btn" color="primary" @click="showFilters = !showFilters"><v-icon>filter_list</v-icon></v-btn>
		</v-layout>
		<directory-filters v-show="showFilters" :dirData="data[0]" @filterSearch="filterSearch" @newFilters="newFilters"></directory-filters>
		<v-data-table-custom
				:headers="headers"
				:items.sync="data"
				:loading="load"
				:total-items="total"
				:pagination.sync="pagination"
				:rows-per-page-items="pages"
				class="mx-4"
				v-model="selected"
				select-all
		>
			<template v-slot:items="props">
				<tr>
					<td>
						<v-checkbox v-model="props.selected" primary hide-details></v-checkbox>
					</td>
					<td>{{props.item.id}}</td>
					<td><p class="maxWidthURL">{{props.item.resourceValue}}</p></td>
					<td>{{props.item.includeTime|dateFilter}}</td>
					<td>{{props.item.resourceType}}</td>
					<td>{{props.item.decisionOrg}}</td>
					<td>{{props.item.infoTypeId}}</td>
					<td>{{props.item.registryName}}</td>
					<td>{{props.item.categoryName}}</td>
					<td>{{props.item.violationName}}</td>
				</tr>
			</template>
			<template v-slot:no-data v-if="!load">
				<v-alert :value="true" color="warning" icon="warning">
					Нет данных для отображения.
				</v-alert>
			</template>
		</v-data-table-custom>
	</v-layout>
</template>

<script>
	import DirectoryFilters from "../components/DirectoryFilters";
	import moment from "moment";

    export default {
        name: "ErdiTable",

		components: {DirectoryFilters},

        props: ['full', 'id', 'search'],

        data() {
            return {
				headers:  [
                    {text: 'ID', align: 'left', value: 'id'},
                    {text: 'Указатель страницы сайта', align: 'left', value: 'resourceValue', sortable: false},
					{text: 'Дата включения в ЕРДИ', value: 'includeTime', sortable: false},
                    {text: 'Тип блокировки', align: 'left', value: 'resourceType',sortable: false},
                    {text: 'Орган, принявший решение', align: 'left', value: 'decisionOrg',sortable: false},
                    {text: 'Тип', align: 'left', value: 'infoTypeId',sortable: false},
                    {text: 'Реестр', align: 'left', value: 'registryName',sortable: false},
                    {text: 'Нарушение', align: 'left', value: 'categoryName',sortable: false},
                    {text: 'ID нарушения', align: 'left', value: 'violationName',sortable: false},
                ],
                data: [],
                total: 0,
                pages: [10, 50, 100, 250 , 500],
                pagination: {
                    sortBy: 'id', descending: true,
                },
                rowsPerPage: null,
                selected: [],
                load: false,

				showFilters: false,
				filtersData: [],
				filtersForAdding: [],
				haveWrongField: false,
            }
        },

        watch: {
            pagination: {
                handler(v) {
                    this.nextPage();
                },
                deep: true
            },
            search() {
                this.nextPage()
            }
        },

		filters: {
			dateFilter(v) {
				if (v == null || v === '')
					return '';
				else {
					return moment(v).locale('ru').format('DD.MM.YYYY, HH:mm:ss');
				}
			}
		},

		methods: {
			filterSearch(filters) {
				this.filtersData = filters;
				this.nextPage();
			},

            nextPage() {
                this.data = [];
                this.load = true;
                const request = this.full ? this.nextPageFullErdi(this.pagination) : this.nextPageFormal(this.pagination);
                request.then(resp=>{
                    this.total = resp.data.totalElements;
                    this.data = resp.data.content;
                }).catch(error=>{
                    console.log('error erdi 1: ', error);
                }).finally(()=>{
                    this.load = false;
                })
            },

            // Upper table, for traffic ID
            nextPageFormal({page, rowsPerPage, sortBy, descending}) {
                let params = {
                    containsInTraffic: true,
                    erdiTrafficUnitId: this.id,
                    pageSize: rowsPerPage,
                    pageNumber: page-1,
                    sortingColumn: sortBy,
                    sortingDirection: this.sortTable(descending),
                };

				if (this.filtersData.filters) {
					this.filtersData.filters.forEach(obj => {
						params[obj.field] = Array.isArray(obj.value) ? obj.value.join(',') : obj.value;
					});
				}
				/*if (this.filtersData.userCount) {
					params.pageSize = this.filtersData.userCount;
					//тут есть проблема с отображением кол-ва строк на странице
					//this.pagination.rowsPerPage = this.filtersData.userCount;
				}*/

                return this.$axios.get(this.$urls.ERDI_FORMAL, {params})
            },

            // Full table, without ID
            nextPageFullErdi({page, rowsPerPage, sortBy, descending}) {
                let params = {
                    query: this.search,
                    containsInTraffic: false,
                    pageSize: rowsPerPage,
                    pageNumber: page - 1,
                    sortingColumn: sortBy,
                    sortingDirection: this.sortTable(descending),
                };

				if (this.filtersData.filters) {
					this.filtersData.filters.forEach(obj => {
						params[obj.field] = Array.isArray(obj.value) ? obj.value.join(',') : obj.value;
					});
				}
				/*if (this.filtersData.userCount) {
					params.pageSize = this.filtersData.userCount;
					//тут есть проблема с отображением кол-ва строк на странице
					//this.pagination.rowsPerPage = this.filtersData.userCount;
				}*/

                return this.$axios.get(this.$urls.POD_ERDI, {params})
            },

            sortTable(descending){
                if(descending!=null){
                    if (descending)
                        return 'DESC'
                    else return 'ASC'
                }else return descending;
            },

            add(check, quantity){
				if(this.haveWrongField) return;

				let data = "";
				let url = "";
				
				// if(this.selected.length){
				if(!check){
					url = `${this.$axios.defaults.baseURL}${this.$urls.ERDI_UNIT}${this.id}/add`;
					data = this.selected.map(item=>item.id);
				}else{
					let query = "";
					if(this.filtersForAdding.length) {
						this.filtersForAdding.forEach((elem) => {
							query += (query) ? "&" : "?";
							query += elem.field + "=";

							if(!elem.value) return;
							if(typeof(elem.value) !== "string"){
								elem.value.forEach((val, index) => {
									if (index)
										query += ",";
									query += val;
								});
							}else{
								query += elem.value;
							}
						});
					}
					let size = (quantity === null || +quantity === 0) ? "" : ((query) ? "&" : "?") + "size=" + quantity;
					url = `${this.$axios.defaults.baseURL}${this.$urls.ERDI_UNIT}${this.id}/addFromPod` + query + size;
				}

                this.$axios.put(url,data).then(()=>{
                    this.$router.push({name: 'trafficinfo', params: {id: this.$route.params.traffic_id}});
                }).catch(error=>{
                    console.log('error custom: ', error);
                });
            },

            delete(){
                let url = `${this.$axios.defaults.baseURL}${this.$urls.ERDI_UNIT}${this.id}/remove`;
                this.$axios.put(url,this.selected.map(item=>item.id)).then(()=>{
                    this.nextPage(this.pagination);
                }).catch(error=>{
                    console.log('error custom: ', error);
                });
            },

			newFilters(event) {
				this.filtersForAdding = event.filters;
				this.haveWrongField = event.validate;
			}
		}
    }
</script>

<style scoped>
	.maxWidthURL{
		word-wrap: break-word;
		max-width: 500px;
	}
</style>