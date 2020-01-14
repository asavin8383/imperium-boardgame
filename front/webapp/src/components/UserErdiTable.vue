<template xmlns:v-slot="http://www.w3.org/1999/XSL/Transform">
	<v-data-table-custom
			:headers="headers"
			:items.sync="data"
			:search="search"
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
				<td><v-checkbox v-model="props.selected" primary hide-details></v-checkbox></td>
				<td>{{props.item.id}}</td>
				<td>{{props.item.name}}</td>
				<td>{{props.item.unitValue}}</td>
				<td>{{props.item.unitType}}</td>
			</tr>
		</template>
		<template v-slot:no-data v-if="!load">
			<v-alert :value="true" color="warning" icon="warning">
				Нет данных для отображения.
			</v-alert>
		</template>
	</v-data-table-custom>
</template>

<script>
    export default {
        name: "UserErdiTable",

        props: ['isHave', 'id', 'search'],

        data() {
            return {
                headers:  [
                    {text: 'ID', align: 'left', value: 'id'},
                    {text: 'Имя', align: 'left', value: 'name',sortable: false},
                    {text: 'Указатель страницы сайта', align: 'left', value: 'unitValue',sortable: false},
                    {text: 'Тип блокировки', align: 'left', value: 'unitType',sortable: false},
                ],
                data: [],
                total: 0,
                pages: [10, 50, 100, 250 , 500],
                pagination: {
                    sortBy: 'id', descending: true,
                },
                page: null,
                rowsPerPage: null,
                sortBy: null,
                descending: null,
                selected: [],
                load: false,
            }
        },

        watch: {
            pagination: {
                handler(v) {
                    this.nextPage();
                },
                deep: true
            }
        },

        methods: {
            nextPage() {
                this.data = [];
                this.load = true;
                const erdiTrafficUnitId = this.isHave ? this.id : null;
                let params = {
                    erdiTrafficUnitId,
                    pageSize: this.pagination.rowsPerPage,
                    pageNumber: this.pagination.page - 1,
                    sortingColumn: this.pagination.sortBy,
                    sortingDirection: this.sortTable(this.pagination.descending),
                };
                const config = () => ({params: params});
                this.$axios.get(this.$urls.ERDI_CUSTOM,config()).then(resp=>{
                        this.data = resp.data.content;
                        this.total = resp.totalElements;
                        this.load = false;
                }).catch(error=>{
                    console.log('error custom: ', error);
                });


            },

            sortTable(descending){
                if(descending!=null){
                    if (descending)
                        return 'DESC';
                    else return 'ASC'
                }else return descending;
            },

            add(){
                let url = `${this.$axios.defaults.baseURL}${this.$urls.CUSTOM_UNIT}${this.id}/add`;
                this.$axios.put(url,this.selected.map(item=>item.id)).then(()=>{
                    this.$router.push({name: 'trafficinfo', params: {id: this.$route.params.traffic_id}});
                }).catch(error=>{
                    console.log('error custom: ', error);
                });
            },

			delete(){
                let url = `${this.$axios.defaults.baseURL}${this.$urls.CUSTOM_UNIT}${this.id}/remove`;
                this.$axios.put(url,this.selected.map(item=>item.id)).then(()=>{
                    this.nextPage(this.page,this.rowsPerPage,this.sortBy,this.descending);
                }).catch(error=>{
                    console.log('error custom: ', error);
                });
			},
        }
    }
</script>

<style scoped>

</style>